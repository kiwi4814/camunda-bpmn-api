
package me.corningrey.camunda.api.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import me.corningrey.camunda.api.model.DefinedSettings;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.service.ProcessSettingApiService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskMonitorService {
    @Resource
    private DefinedSettings definedSettings;
    @Resource
    private ProcessSettingApiService processSettingApiService;
    /*@Resource
    private ProcessSettingService processSettingService;*/
    /**
     * 用来存放每次发送待办的审批人列表，在一次发送待办中无需重复调用接口
     */
    static ThreadLocal<List<String>> listThreadLocal = new ThreadLocal<>();
    private final static String KEY = "taskDefinitionKey";
    private final static String NAME = "taskDefinitionName";
    private final static String TYPE = "taskDefinitionType";

    public List<String> initTask(DelegateExecution execution) throws Exception {
        List<String> approverList;
        Integer loopCounter = (Integer) execution.getVariable("loopCounter");
        if (loopCounter == null) {
            Map<String, String> map = initTaskDefinitionInfo(execution);
            String taskDefinitionKey = map.get(KEY);
            String optionalUsers = getFinalOptionalUsers(execution, taskDefinitionKey);
            approverList = getApproverList(execution, optionalUsers, map, null);
            initDataInLoopStart(execution, taskDefinitionKey, approverList);
            return approverList;
        }
        approverList = listThreadLocal.get();
        if (loopCounter == 0) {
            initDataInLoopEnd(execution);
        }
        return approverList;
    }

    public List<String> initTask(DelegateExecution execution, Object dynamicVar) throws Exception {
        if (dynamicVar == null) {
            throw new RuntimeException("ERROR-501：节点中配置的审批人变量不存在！");
        }
        List<String> approverList;
        Integer loopCounter = (Integer) execution.getVariable("loopCounter");
        if (loopCounter == null) {
            Map<String, String> map = initTaskDefinitionInfo(execution);
            String taskDefinitionKey = map.get(KEY);
            String optionalUsers = getFinalOptionalUsers(execution, taskDefinitionKey);
            approverList = getApproverList(execution, optionalUsers, map, dynamicVar);
            initDataInLoopStart(execution, taskDefinitionKey, approverList);
            return approverList;
        }
        approverList = listThreadLocal.get();
        if (loopCounter == 0) {
            initDataInLoopEnd(execution);
        }
        return approverList;
    }

    /**
     * 获取审批人列表
     *
     * @param execution     执行实例
     * @param optionalUsers 自选审批人
     * @param map           节点定义Key、Type、Name
     * @return 审批人列表
     * @throws Exception 审批人为空时根据设置信息，可能会抛出异常
     */
    private List<String> getApproverList(DelegateExecution execution, String optionalUsers, Map<String, String> map, Object dynamicVar) throws Exception {
        List<String> approverList;
        String taskDefinitionKey = map.get(KEY);
        if (StringUtils.isNotBlank(optionalUsers)) {
            approverList = (List<String>) Convert.toList(optionalUsers);
            if (isPlusSignActivity(execution, taskDefinitionKey)) {
                List<String> defaultApprovers = getDefaultApproverList(execution, taskDefinitionKey, dynamicVar);
                if (defaultApprovers != null && !defaultApprovers.isEmpty()) {
                    approverList.addAll(defaultApprovers);
                }
            }
        } else {
            approverList = getDefaultApproverList(execution, taskDefinitionKey, dynamicVar);
        }
        if (approverList == null || approverList.isEmpty()) {
            checkIsPassEmptyNode(execution, map);
            approverList = new ArrayList<>();
        }
        return approverList;
    }


    /**
     * 根据流程配置获取预设审批人信息
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 节点定义Key
     * @return 审批人列表
     */
    private List<String> getDefaultApproverList(DelegateExecution execution, String taskDefinitionKey, Object dynamicVar) throws Exception {
        // 这里无需判断list是否是空，只需要判断非null即可，因为客户端也有可能传空
        if (dynamicVar != null) {
            return (List<String>) Convert.toList(dynamicVar);
        }
        // 获取接口的参数列表,根据设置信息，查找审批人
        Map<String, String> paramMap = CamundaUtil.filterVariablesMap(execution.getVariables(), definedSettings.getFilterVariables());
        paramMap.put("businessKey", execution.getProcessBusinessKey());
        paramMap.put("processInstanceId", execution.getProcessInstanceId());
        paramMap.put(BpmnVariableConstant.OPTIONAL_IDS, getFinalOptionalIds(execution, taskDefinitionKey));
        return processSettingApiService.getUserListByTaskSetting(execution.getProcessDefinitionId(), taskDefinitionKey, paramMap);
    }

    /**
     * 第一次循环时，将流程关键信息保存到流程变量中
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 任务节点Key
     * @param approverList      审批人列表
     */
    private void initDataInLoopStart(DelegateExecution execution, String taskDefinitionKey, List<String> approverList) throws UnitedException {
        Map<String, Object> startVariables = new HashMap<>(8);
        // 保存节点类型、审批选项、操作选项等
        /*TaskSettingHis taskSetting = processSettingService.selectTaskSettingByConditions(execution.getProcessDefinitionId(), taskDefinitionKey);
        if (taskSetting != null) {
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_TASKKEY_NODETYPE), taskSetting.getTaskSettingType());
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_APPROVEOPTIONS), taskSetting.getApproveOptions());
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_OPERACTIONS), taskSetting.getOperActions());
        }*/
        // 自选审批人
        startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_APPROVERS), approverList);
        // 是否为节点最后一个审批人
        if (approverList.size() == 1) {
            startVariables.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER), "1");
        }
        listThreadLocal.set(approverList);
        execution.setVariables(startVariables);
    }

    /**
     * 获取当前节点的节点定义Key、节点类型、节点名称
     *
     * @param execution 执行实例
     */
    private Map<String, String> initTaskDefinitionInfo(DelegateExecution execution) {
        Map<String, String> initVariables = new HashMap<>(8);
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        ActivityImpl activityImpl = executionEntity.getActivity();
        if (activityImpl != null) {
            List<ActivityImpl> activities = activityImpl.getActivities();
            if (activities != null) {
                ActivityImpl activity = activities.get(0);
                initVariables.put(KEY, activity.getActivityId());
                initVariables.put(NAME, activity.getName());
                initVariables.put(TYPE, (String) activity.getProperties().toMap().get("type"));
                return initVariables;
            }
        }
        throw new RuntimeException("ERROR-502：未找到节点属性！");
    }

    /**
     * 判断节点是否为加签节点（在原有审批人上新增自选审批人而非替换）
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 节点定义Key
     * @return 是否加签
     */
    private Boolean isPlusSignActivity(DelegateExecution execution, String taskDefinitionKey) {
        Object o1 = execution.getVariable(BpmnVariableConstant.IS_PLUS_SIGN);
        if (o1 != null) {
            return Convert.toBool(o1);
        } else {
            Object o2 = execution.getVariable(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_PLUS_SIGN));
            return o2 == null ? false : Convert.toBool(o2);
        }
    }

    /**
     * 获取自选审批人
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 节点定义Key
     * @return 自选审批人
     */
    private String getFinalOptionalUsers(DelegateExecution execution, String taskDefinitionKey) {
        String optionalUsers = getVariableStr(execution, BpmnVariableConstant.OPTIONAL_USERS, null);
        if (StringUtils.isBlank(optionalUsers)) {
            optionalUsers = getVariableStr(execution, BpmnVariableConstant.SUFFIX_REPLACED_USERS, taskDefinitionKey);
        }
        return optionalUsers;
    }

    /**
     * 获取自选审批人
     *
     * @param execution         执行实例
     * @param taskDefinitionKey 节点定义Key
     * @return 自选审批人
     */
    private String getFinalOptionalIds(DelegateExecution execution, String taskDefinitionKey) {
        String optionalIdss = getVariableStr(execution, BpmnVariableConstant.OPTIONAL_IDS, null);
        if (StringUtils.isBlank(optionalIdss)) {
            optionalIdss = getVariableStr(execution, BpmnVariableConstant.SUFFIX_REPLACED_IDS, taskDefinitionKey);
        }
        return optionalIdss;
    }

    /**
     * 当审批人为空时，通过流程变量判断流程是否继续执行（跳过空节点/返回异常）
     *
     * @param execution 执行实例
     */
    private void checkIsPassEmptyNode(DelegateExecution execution, Map<String, String> map) {
        Boolean isPassEmptyNode;
        Object o1 = execution.getVariable(BpmnVariableConstant.IS_PASS_EMPTY_NODE);
        if (o1 != null) {
            isPassEmptyNode = Convert.toBool(o1);
        } else {
            String taskDefinitionKey = map.get(KEY);
            Object o2 = execution.getVariable(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_PASS_EMPTY_NODE));
            isPassEmptyNode = o2 == null ? false : Convert.toBool(o2);
        }
        String userTask = "userTask";
        if (!isPassEmptyNode && StrUtil.equals(userTask, map.get(TYPE))) {
            throw new RuntimeException(String.format("ERROR-503：【%s】节点用户不存在", map.get(NAME)));
        }
    }

    /**
     * 获取String格式的流程变量
     *
     * @param execution 执行实例
     * @param name      变量名称
     * @param suffix    变量前缀
     * @return variable
     */
    private String getVariableStr(DelegateExecution execution, String name, String suffix) {
        if (StringUtils.isNotBlank(suffix)) {
            name = suffix.concat(name);
        }
        Object o = execution.getVariable(name);
        if (o != null) {
            return Convert.toStr(o);
        }
        return null;
    }

    /**
     * 循环的最后一次时，将全局变量设为空，将临时变量删除
     *
     * @param execution 执行实例
     */
    private void initDataInLoopEnd(DelegateExecution execution) {
        Map<String, String> emptyVariables = new HashMap<>(8);
        // 清空自选审批人变量
        emptyVariables.put(BpmnVariableConstant.OPTIONAL_USERS, "");
        // 清空加签变量
        emptyVariables.put(BpmnVariableConstant.IS_PLUS_SIGN, "");
        // 清空自定义参数变量
        emptyVariables.put(BpmnVariableConstant.OPTIONAL_IDS, "");
        // 清空是否跳过空节点的变量
        emptyVariables.put(BpmnVariableConstant.IS_PASS_EMPTY_NODE, "");
        // 清空临时审批人列表变量
        listThreadLocal.remove();
        execution.setVariables(emptyVariables);
    }
}


