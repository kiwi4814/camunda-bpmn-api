
package me.corningrey.camunda.api.listener;

import cn.hutool.core.util.StrUtil;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import me.corningrey.camunda.api.model.DefinedSettings;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.service.ProcessSettingApiService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @deprecated 已用新的接口TaskMonitorService重构
 */
@Component
@Deprecated
public class ApproverParsingService {
    @Resource
    private DefinedSettings definedSettings;
    @Resource
    private ProcessSettingApiService processSettingApiService;
   /* @Resource
    private ProcessSettingService processSettingService;*/

    private String taskDefinitionKey;
    private String taskDefinitionName;
    private String taskDefinitionType;

    /**
     * 流程过节点的时候查找审批人
     *
     * @param execution     流程执行实例
     * @param optionalUsers 自选审批人
     * @param taskSettingId 节点设置ID,自1.2版本已废弃
     * @return 审批人List
     */
    public List<String> findApproverList(DelegateExecution execution, String optionalUsers, @Deprecated String taskSettingId) {
        List<String> approverList;
        try {
            Integer loopCounter = (Integer) execution.getVariable("loopCounter");

            if (loopCounter == null) {
                // 初始化taskDefinitionKey、taskDefinitionName、taskDefinitionType
                Map<String, String> commonMap = getTaskDefinitionKey(execution);
                taskDefinitionKey = commonMap.get("taskDefinitionKey");
                taskDefinitionName = commonMap.get("taskDefinitionName");
                taskDefinitionType = commonMap.get("taskDefinitionType");
                // 处理提前更换了自选审批人或者查询参数的情况（顺序为先判断上个节点的自选审批人变量optionalUsers是否有值，再判断前面节点的预设审批人是否有值，最后判断是否修改了查询参数）
                if (StringUtils.isBlank(optionalUsers)) {
                    handlingChangeApproverInAdvance(execution);
                    optionalUsers = (String) execution.getVariable("optionalUsers");
                }
            }

            // 查找、拼装节点最后确定的审批人
            approverList = getApproverList(execution, optionalUsers, taskDefinitionKey);

            // 创建第一个ActivityInstance时，归档某些历史数据
            if (loopCounter == null) {
                // 处理对于单人审批节点下流程变量（SUFFIX_IS_FINAL_APPROVER）的情况
                if (approverList.size() == 1) {
                    execution.setVariable(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER), "1");
                }
                initDataInLoopStart(execution, taskDefinitionKey, optionalUsers);
            }

            // 创建最后一个ActivityInstance时，清空某些无用的流程变量
            if (loopCounter != null && loopCounter == 0) {
                initDataInLoopEnd(execution);
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            throw new RuntimeException(e);
        }
        return approverList;
    }

    /**
     * 获取审批人
     *
     * @param execution         当前执行实例
     * @param optionalUsers     自选审批人
     * @param taskDefinitionKey 节点定义key
     */
    private List<String> getApproverList(DelegateExecution execution, String optionalUsers, String taskDefinitionKey) throws Exception {
        List<String> approverList = new ArrayList<>();
        String processDefinitionId = execution.getProcessDefinitionId();
        Map<String, String> paramMap = CamundaUtil.filterVariablesMap(execution.getVariables(), definedSettings.getFilterVariables());
        paramMap.put("businessKey", execution.getProcessBusinessKey());
        paramMap.put("processInstanceId", execution.getProcessInstanceId());
        // 流程定义ID
        if (StringUtils.isNotBlank(optionalUsers)) {
            // 取出流程变量“是否加签”，1或者true为加签
            String isPlusSign = String.valueOf(execution.getVariable(BpmnVariableConstant.IS_PLUS_SIGN));
            try {
                if (optionalUsers.startsWith("${") && optionalUsers.endsWith("}")) {
                    ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
                    Object result = expressionManager.createExpression(optionalUsers).getValue(execution);
                    if (result instanceof String) {
                        optionalUsers = String.valueOf(result);
                    } else if (result instanceof ArrayList) {
                        approverList = (List<String>) result;
                    }
                }
            } catch (Exception e) {
                UnitedLogger.info(e);
            }
            if (approverList.isEmpty()) {
                approverList = new ArrayList<>(Arrays.asList(StringUtils.split(optionalUsers, ",")));
            }
            if (StrUtil.equalsAny(isPlusSign, "1", "true")) {
                List<String> defaultApprovers = processSettingApiService.getUserListByTaskSetting(processDefinitionId, taskDefinitionKey, paramMap);
                if (defaultApprovers != null && !defaultApprovers.isEmpty()) {
                    approverList.addAll(defaultApprovers);
                }
            }
        } else {
            approverList = processSettingApiService.getUserListByTaskSetting(processDefinitionId, taskDefinitionKey, paramMap);
        }
        // 取出流程变量“是否跳过空节点”，1或者true为跳过。如果获取不到节点用户并且不能直接跳过，则抛出异常提示信息
        String isPassEmptyNode = String.valueOf(execution.getVariable(BpmnVariableConstant.IS_PASS_EMPTY_NODE));
        // 只有用户节点才判断节点审批人为空的情况
        if ("userTask".equals(taskDefinitionType)) {
            if (approverList.isEmpty() && !StringUtils.equals("1", isPassEmptyNode) && !StringUtils.equalsIgnoreCase("true", isPassEmptyNode)) {
                throw new RuntimeException(String.format("【%s】节点用户不存在", taskDefinitionName));
            }
        }
        return approverList;
    }

    /**
     * 创建第一个ActivityInstance时，执行节点属性变量初始化操作
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 节点设置ID
     */
    private void initDataInLoopStart(DelegateExecution execution, String taskDefinitionKey, String optionalUsers) throws UnitedException {
        Map<String, Object> startVariables = new HashMap<>(9);
        /*TaskSettingHis taskSetting = processSettingService.selectTaskSettingByConditions(execution.getProcessDefinitionId(), taskDefinitionKey);
        if (taskSetting != null) {
            // 初始化节点的设置ID，节点类型和节点的可选审批项
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_TASKKEY_NODEID), taskSetting.getTaskSettingId());
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_TASKKEY_NODETYPE), taskSetting.getTaskSettingType());
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_APPROVEOPTIONS), taskSetting.getApproveOptions());
        }*/
        // 归档历史数据
        if (StringUtils.isNotBlank(optionalUsers)) {
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_OPTIONAL_USERS), optionalUsers);
        }
        Object isPlusSign = execution.getVariable(BpmnVariableConstant.IS_PLUS_SIGN);
        if (isPlusSign != null) {
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_PLUS_SIGN), isPlusSign);
        }
        Object optionalIds = execution.getVariable(BpmnVariableConstant.OPTIONAL_IDS);
        if (optionalIds != null) {
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_OPTIONAL_IDS), optionalIds);
        }
        execution.setVariables(startVariables);
    }

    /**
     * 创建最后一个ActivityInstance时，清空通用的流程变量
     *
     * @param execution 执行实例
     */
    private void initDataInLoopEnd(DelegateExecution execution) {
        Map<String, String> endVariables = new HashMap<>(8);
        // 清空自选审批人变量
        endVariables.put(BpmnVariableConstant.OPTIONAL_USERS, "");
        // 清空加签变量
        endVariables.put(BpmnVariableConstant.IS_PLUS_SIGN, "");
        // 清空自定义参数变量
        endVariables.put(BpmnVariableConstant.OPTIONAL_IDS, "");
        // 清空是否跳过空节点的变量
        endVariables.put(BpmnVariableConstant.IS_PASS_EMPTY_NODE, "");
        execution.setVariables(endVariables);
        taskDefinitionKey = "";
    }

    /**
     * 处理在当前流程实例在此节点之前是否为此节点预设了审批人
     *
     * @param execution 当前执行实例
     */
    private void handlingChangeApproverInAdvance(DelegateExecution execution) {
        Map<String, Object> addVariables = new HashMap<>(8);
        // 以下涉及到removeVariables的流程变量移除操作暂时注释
        // 以便流程跳转时这些流程变量可以复用
        // List<String> removeVariables = new ArrayList<>();
        String replacedUsersKey = taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_REPLACED_USERS);
        Object replacedUsersValue = execution.getVariable(replacedUsersKey);
        if (replacedUsersValue != null) {
            addVariables.put(BpmnVariableConstant.OPTIONAL_USERS, replacedUsersValue);
            // removeVariables.add(replacedUsersKey);
        } else {
            String replacedIdsKey = taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_REPLACED_IDS);
            Object replacedIdsValue = execution.getVariable(replacedIdsKey);
            if (replacedIdsValue != null) {
                addVariables.put(BpmnVariableConstant.OPTIONAL_IDS, replacedIdsValue);
                // removeVariables.add(replacedIdsKey);
            }
        }
        String isPassEmptyNodeKey = taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_PASS_EMPTY_NODE);
        Object isPassEmptyNodeValue = execution.getVariable(isPassEmptyNodeKey);
        if (isPassEmptyNodeValue != null) {
            addVariables.put(BpmnVariableConstant.IS_PASS_EMPTY_NODE, isPassEmptyNodeValue);
            // removeVariables.add(isPassEmptyNodeKey);
        }
        execution.setVariables(addVariables);
        // execution.removeVariables(removeVariables);
    }


    /**
     * 或者当前节点的taskDefinitionKey
     *
     * @param execution 执行进程
     * @return taskDefinitionKey
     */
    private Map<String, String> getTaskDefinitionKey(DelegateExecution execution) {
        Map<String, String> commonMap = new HashMap<>(8);
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        ActivityImpl activityImpl = executionEntity.getActivity();
        if (activityImpl != null) {
            List<ActivityImpl> activities = activityImpl.getActivities();
            if (activities != null) {
                ActivityImpl activity = activities.get(0);
                commonMap.put("taskDefinitionName", activity.getName());
                commonMap.put("taskDefinitionKey", activity.getActivityId());
                commonMap.put("taskDefinitionType", (String) activity.getProperties().toMap().get("type"));
            }
        }
        return commonMap;
    }

}
