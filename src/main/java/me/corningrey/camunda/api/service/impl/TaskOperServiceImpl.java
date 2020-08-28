
package me.corningrey.camunda.api.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.cmd.CheckCompletionConditionCmd;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.enums.TaskStatusEnum;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.service.TaskAgentService;
import me.corningrey.camunda.api.service.TaskOperService;
import me.corningrey.camunda.api.util.*;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskOperServiceImpl implements TaskOperService {
    @Resource
    private TaskService taskService;
    @Resource
    private TaskAgentService taskAgentService;
    @Resource
    private ProcessOperHistoryService processOperHistoryService;
    @Resource
    private CommandExecutor commandExecutor;
    @Resource
    private RuntimeService runtimeService;

    @Override
    public Map<String, Object> findTasks(TaskSearch taskSearch) {
        // 租户id
        String tenantId = taskSearch.getTenantId();
        // 流程实例id
        String instanceId = taskSearch.getInstanceId();
        // 审批人
        String userId = taskSearch.getUserId();
        // 节点定义Key
        String taskDefinitionKey = taskSearch.getTaskDefinitionKey();
        // 流程定义Key
        String processDefinitionKey = taskSearch.getProcessDefinitionKey();
        // businessKey
        String businessKey = taskSearch.getBusinessKey();
        // 流程实例名称
        String instanceRemark = taskSearch.getInstanceRemark();
        // 待办状态
        String finishedStatus = taskSearch.getFinishedStatus();

        HistoricTaskInstanceQuery query = ServiceUtil.getHistoricTaskInstanceQuery();
        // 设置待办查询条件
        if (StringUtils.isNotBlank(tenantId)) { // 租户id
            query.tenantIdIn(tenantId);
        }
        if (StringUtils.isNotBlank(instanceId)) { // 流程实例id
            query.processInstanceId(instanceId);
        }
        if (StringUtils.isNotBlank(userId)) { // 审批人
            query.taskAssignee(userId);
        }
        if (StringUtils.isNotBlank(taskDefinitionKey)) { // 节点定义Key
            query.taskDefinitionKey(taskDefinitionKey);
        }
        if (StringUtils.isNotBlank(processDefinitionKey)) {
            query.processDefinitionKey(processDefinitionKey);
        }
        if (StringUtils.isNotBlank(businessKey)) {
            query.processInstanceBusinessKey(businessKey);
        }
        if (StringUtils.isNotBlank(instanceRemark)) {
            instanceRemark = CamundaUtil.decode(instanceRemark, StandardCharsets.UTF_8.name());
            query.processVariableValueLike(BpmnVariableConstant.INST_REMARK, "%" + StringUtils.trim(instanceRemark) + "%");
        }
        if (StringUtils.equals(finishedStatus, "0")) {
            query.finished();
        } else if (StringUtils.equals(finishedStatus, "1")) {
            query.unfinished();
        }

        // 如果不为1，则只查询处于“已完成”状态的；为1，查询已完成+未完成
        if (!"1".equals(taskSearch.getOperStatus())) {
            query.finished();
        }

        // 按照待办开始时间降序排列
        query.orderByHistoricActivityInstanceStartTime().desc();

        // 查询用户的所有历史待办
        // 开始分页
        Map<String, Object> resultMap = CamundaUtil.getCamundaParamer(taskSearch.getPageNum(), taskSearch.getPageLimit(), query);
        List<HistoricTaskInstance> tasks = (List<HistoricTaskInstance>) resultMap.get("list");
        List<TaskExt> tlist = new ArrayList<>();
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(t -> tlist.add(getTaskExtByBpmnTask(t.getProcessDefinitionId(),
                    t.getProcessInstanceId(),
                    t.getId(),
                    t.getTaskDefinitionKey(), taskSearch.getVariableKeys())));
        }
        resultMap.put("list", tlist);
        return resultMap;
    }

    @Override
    public List<TaskExt> findToDoTasks(TaskSearch taskSearch) throws UnitedException {
        // 租户id
        String tenantId = taskSearch.getTenantId();
        // 流程实例id
        String instanceId = taskSearch.getInstanceId();
        // 审批人
        String userId = taskSearch.getUserId();
        // 流程定义Key
        String processDefinitionKey = taskSearch.getProcessDefinitionKey();
        // businessKey
        String businessKey = taskSearch.getBusinessKey();
        // 流程实例名称
        String instanceRemark = taskSearch.getInstanceRemark();

        TaskQuery taskQuery = ServiceUtil.getTaskQuery();
        // 设置待办查询条件
        if (StringUtils.isNotBlank(tenantId)) { // 租户id
            taskQuery.tenantIdIn(tenantId);
        }
        if (StringUtils.isNotBlank(instanceId)) { // 流程实例id
            taskQuery.processInstanceId(instanceId);
        }
        if (StringUtils.isNotBlank(userId)) { // 审批人
            taskQuery.taskAssignee(userId);
        }
        if (StringUtils.isNotBlank(processDefinitionKey)) {
            taskQuery.processDefinitionKey(processDefinitionKey);
        }
        if (StringUtils.isNotBlank(businessKey)) {
            taskQuery.processInstanceBusinessKey(businessKey);
        }
        if (StringUtils.isNotBlank(instanceRemark)) {
            instanceRemark = CamundaUtil.decode(instanceRemark, StandardCharsets.UTF_8.name());
            taskQuery.processVariableValueLike(BpmnVariableConstant.INST_REMARK, "%" + StringUtils.trim(instanceRemark) + "%");
        }
        // 查询用户的待办任务
        List<Task> tasks = taskQuery.active().list();

        // 查询用户的代理任务
        if (StringUtils.isNotBlank(userId)) {
            // 如果传了审批人，则顺便把代理任务也查出来
            taskSearch.setAgent(userId);
        }
        List<TaskAgent> agentTasks = taskAgentService.findAgentInfo(taskSearch);

        // 将上面两个list整合到taskExts
        List<TaskExt> taskExts = new ArrayList<>();
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(t -> taskExts.add(getTaskExtByBpmnTask(t.getProcessDefinitionId(),
                    t.getProcessInstanceId(),
                    t.getId(),
                    t.getTaskDefinitionKey(),
                    taskSearch.getVariableKeys())));
        }
        if (CollUtil.isNotEmpty(agentTasks)) {
            agentTasks.forEach(at -> {
                Task t = taskService.createTaskQuery().taskId(at.getTaskId()).singleResult();
                if (t != null) {
                    taskExts.add(getTaskExtByBpmnTask(t.getProcessDefinitionId(),
                            t.getProcessInstanceId(),
                            t.getId(),
                            t.getTaskDefinitionKey(),
                            taskSearch.getVariableKeys()));
                }
            });
        }
        taskExts.sort(Comparator.comparing(TaskExt::getStartTime).reversed());
        return taskExts;
    }

    @Override
    public TaskExt getTaskExtByBpmnTask(String processDefinitionId, String processInstanceId, String taskId, String taskDefinitionKey, String variableKeys) {
        //代理人
        TaskAgent taskAgent = taskAgentService.getAgentTask(taskId);
        String agent = null;
        if (taskAgent != null) {
            agent = taskAgent.getAgent();
        }
        // processDefinitionId, processInstanceId, taskId, taskDefinitionKey

        // 流程定义名称
        ProcessDefinition pd = RuntimeServiceUtil.findProcessDefinitionById(processDefinitionId);
        HistoricVariableInstanceQuery hiq = ServiceUtil.getHistoricVariableInstanceQuery().processInstanceId(processInstanceId);
        // 任务审批动作
        HistoricVariableInstance actionV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskId + BpmnVariableConstant.SUFFIX_ACTION);
        String action = actionV != null ? (String) actionV.getValue() : null;
        // 任务审批意见
        HistoricVariableInstance commentV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskId + BpmnVariableConstant.SUFFIX_COMMENT);
        String comment = commentV != null ? (String) commentV.getValue() : null;
        // 任务对应的实例名称
        HistoricVariableInstance instanceRemarkV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, BpmnVariableConstant.INST_REMARK);
        String instanceRemark = Optional.ofNullable(instanceRemarkV).map(HistoricVariableInstance::getValue).map(String::valueOf).orElse(pd.getName());
        instanceRemark = StringUtils.isNotBlank(instanceRemark) ? instanceRemark : pd.getName();
        // 任务对应的节点设置ID（拓展表的ID字段）
        HistoricVariableInstance taskSettingIdV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskDefinitionKey + BpmnVariableConstant.SUFFIX_TASKKEY_NODEID);
        String taskSettingId = taskSettingIdV != null ? (String) taskSettingIdV.getValue() : null;
        // 任务对应的节点设置类型（拓展表的类型字段）
        HistoricVariableInstance taskSettingTypeV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskDefinitionKey + BpmnVariableConstant.SUFFIX_TASKKEY_NODETYPE);
        String taskSettingType = taskSettingTypeV != null ? (String) taskSettingTypeV.getValue() : null;
        // 任务对应的节点设置类型（拓展表的审批可选项字段）
        HistoricVariableInstance approveOptionsV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskDefinitionKey + BpmnVariableConstant.SUFFIX_APPROVEOPTIONS);
        String approveOptions = approveOptionsV != null ? (String) approveOptionsV.getValue() : null;
        HistoricVariableInstance operActionsV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskDefinitionKey + BpmnVariableConstant.SUFFIX_OPERACTIONS);
        String operActions = operActionsV != null ? (String) operActionsV.getValue() : null;
        HistoricTaskInstance t = HistoryServiceUtil.getHistoricTaskInstanceByTaskId(taskId);
        String status;
        if (t.getEndTime() == null) {
            if (RuntimeServiceUtil.isSuspended(processInstanceId)) {
                status = TaskStatusEnum.suspended.getCode();
            } else {
                status = TaskStatusEnum.unfinished.getCode();
            }
        } else if (t.getDeleteReason().endsWith(BpmnVariableConstant.SUFFIX_CANCEL_REASON)) {
            status = TaskStatusEnum.canceled.getCode();
        } else {
            status = TaskStatusEnum.finished.getCode();
        }

        // 获取待办关联流程的申请人
        HistoricVariableInstance applyerIdV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, "applicant");
        String applyerId = applyerIdV != null ? (String) applyerIdV.getValue() : null;
        // 获取关联的流程实例
        HistoricProcessInstance hpi = HistoryServiceUtil.getHistoricProcessInstanceByInstanceId(processInstanceId);
        TaskExt taskExt = TaskExt.builder().taskId(t.getId()).applyerId(applyerId).assignee(t.getAssignee()).agent(agent).result(action).comment(comment).startTime(t.getStartTime()).endTime(t.getEndTime())
                .status(status).taskDefinitionKey(t.getTaskDefinitionKey()).taskDefinitionName(t.getName()).taskSettingId(taskSettingId).taskSettingType(taskSettingType).approveOptions(approveOptions).operActions(operActions)
                .instanceId(t.getProcessInstanceId()).executionId(t.getExecutionId()).instanceRemark(instanceRemark)
                .processDefinitionId(t.getProcessDefinitionId()).processDefinitionKey(t.getProcessDefinitionKey()).processDefinitionName(pd.getName()).processInstanceStartTime(hpi.getStartTime()).build();

        // 和待办关联的流程变量，支持taskId和taskDefinitionKey作为前缀的流程变量
        if (StringUtils.isNotBlank(variableKeys)) {
            variableKeys = CamundaUtil.decode(variableKeys, StandardCharsets.UTF_8.name());
            HistoricVariableInstanceQuery variableQuery = HistoryServiceUtil.getHistoricVariableInstanceQuery().processInstanceId(processInstanceId);
            Map<String, Object> variableMap = new HashMap<>(16);
            variableKeys = StringUtils.replace(variableKeys, "{taskId}", taskId);
            variableKeys = StringUtils.replace(variableKeys, "{taskDefinitionKey}", taskDefinitionKey);
            List<String> variableList = Arrays.asList(StringUtils.split(variableKeys, ","));
            variableList.forEach(k -> {
                HistoricVariableInstance v = variableQuery.variableName(k).singleResult();
                variableMap.put(k, Optional.ofNullable(v).map(HistoricVariableInstance::getValue).orElse(""));
            });
            taskExt.setVariableMap(new JSONObject(variableMap));
        }


        return taskExt;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void audit(String taskId, String assignee, String action, String comment,
                      Integer completionLevel, String optionalUsers, Map<String, Object> variables) throws UnitedException {
        //判断是否是代理任务，如果有有代理记录，完成这条代理记录
        TaskAgent taskAgent = taskAgentService.getAgentTask(taskId);
        if (taskAgent != null) {
            taskAgentService.finishTask(taskId, assignee);
        }

        // 完成流程引擎的待办
        Task task = taskService.createTaskQuery().taskId(taskId).active().singleResult();
        if (task == null) {
            throw new UnitedException("待办已被审批或者冻结，请刷新页面后查看");
        } else {
            // 校验action是否在配置的选项范围内，如果满足要求，返回该节点的配置审批选项，用于后面初始化流程变量
            List<String> codeList = checkActionValid(taskId, task.getTaskDefinitionKey(), action);
            // 查找该节点的节点类型【用户审批节点、自选审批人节点】
            Object taskSettingTypeObj = taskService.getVariable(taskId, task.getTaskDefinitionKey().concat(BpmnVariableConstant.SUFFIX_TASKKEY_NODETYPE));
            String taskSettingType = Optional.ofNullable(taskSettingTypeObj).map(Object::toString).orElse("");
            // 只要不是自选审批人，都会进入计票的方法，因为目前只有userTask才会调用completeTask这个方法
            if (!StringUtils.equals(taskSettingType, CommonConstant.OPTIONAL_TASK)) {
                // 会签节点操作：统计多任务节点上的票数
                if (StringUtils.isNotBlank(task.getTaskDefinitionKey())) {
                    calcVotes(taskId, task.getTaskDefinitionKey(), action, variables, codeList);
                }
            }
            // 将当前审批人更新到流程变量中
            variables.put(BpmnVariableConstant.LAST_ASSIGNEE, task.getAssignee());
            //审批意见变量设置
            variables.put(taskId.concat(BpmnVariableConstant.SUFFIX_COMMENT), comment);
            //审批动作变量设置
            variables.put(taskId.concat(BpmnVariableConstant.SUFFIX_ACTION), action);
            //自选审批人
            if (StringUtils.isNotBlank(optionalUsers)) {
                variables.put(BpmnVariableConstant.OPTIONAL_USERS, optionalUsers);
            }
            // 插入操作日志（comment "执行审批操作：" + action + "；审批意见：" + comment）
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);
            jsonObject.put("comment", comment);
            String operType = StringUtils.startsWith(taskId, "SIGN_") ? ProcessOperEnum.AUDIT_SIGN.getValue() : ProcessOperEnum.AUDIT.getValue();
            processOperHistoryService.insertTaskHistory(taskId, operType, assignee, jsonObject.toString(), comment);
            // 批量更新流程变量
            taskService.setVariables(taskId, variables);
            // 如果传入参数completionLevel，并且值为1或者2的时候
            if (completionLevel != null && (completionLevel == 1 || completionLevel == 2)) {
                // 最后一个审批人的时候，判断流程完成条件的表达式能否成立，不成立的话，结束流程
                Object o = taskService.getVariable(taskId, task.getTaskDefinitionKey().concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER));
                String isFinalApprover = Optional.ofNullable(o).map(Object::toString).orElse("");
                if (StringUtils.equals("1", isFinalApprover)) {
                    Boolean result = commandExecutor.execute(new CheckCompletionConditionCmd(taskId));
                    if (!result) {
                        switch (completionLevel) {
                            case 1:
                                runtimeService.suspendProcessInstanceById(task.getProcessInstanceId());
                                return;
                            case 2:
                                runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "unmetCompletion");
                                return;
                        }
                    }
                }
            }
            // 完成待办【之所以不用complete(String taskId, Map<String,Object> variables)是因为这个方法的变量范围是任务所在execution】
            taskService.complete(taskId);
        }

    }

    private List<String> checkActionValid(String taskId, String taskDefinitionKey, String action) throws UnitedException {
        Object options = taskService.getVariable(taskId, taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_APPROVEOPTIONS));
        if (options == null) {
            if (StringUtils.isNotBlank(action)) {
                throw new UnitedException("该节点未配置任何审批选项！");
            }
        } else {
            List<String> codeList = Arrays.asList(StringUtils.split(options.toString(), ","));
            codeList = codeList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (StringUtils.isNotBlank(action) && !codeList.contains(action)) {
                throw new UnitedException("该审批选项无效!");
            }
            return codeList;
        }
        return null;
    }

    /**
     * 统计并更新计票数流程变量
     *
     * @param taskId            待办ID
     * @param taskDefinitionKey 待办定义key
     * @param actionCode        审批选项
     * @param variables         流程变量Map
     * @param codeList          节点审批选项列表
     */
    private void calcVotes(String taskId, String taskDefinitionKey, String actionCode, Map<String, Object> variables, List<String> codeList) {
        // 针对流程中可能出现的循环，根据不同情况做清零及初始化操作
        boolean isMultiTask = TaskServiceUtil.checkIsMultiTask(taskId);
        if (checkNeedClearVotes(taskId, taskDefinitionKey, isMultiTask)) {
            clearVotes(taskId, taskDefinitionKey, isMultiTask, codeList);
        }

        // 更新当前待办的审批结果投票数，如果该节点的配置审批选项为空，则不需要更新
        if (codeList != null) {
            String optionCountVariable = taskDefinitionKey.concat("_").concat(actionCode).concat(BpmnVariableConstant.SUFFIX_COUNT);
            Integer optionCount = (Integer) taskService.getVariable(taskId, optionCountVariable); // 获取当前审批结果已有的票数
            Optional.ofNullable(optionCount).ifPresent(t -> variables.put(optionCountVariable, ++t));
        }

        // 更新当前节点的已完成待办数
        String completeCountVariable = taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_NROFCOMPLETEDINSTANCES);
        Integer completedCount = (Integer) taskService.getVariable(taskId, completeCountVariable);
        Optional.ofNullable(completedCount).ifPresent(t -> variables.put(completeCountVariable, ++t));
    }

    /**
     * 判断票数是否需要清零
     *
     * @param taskId            待办id
     * @param taskDefinitionKey 节点名称
     * @param isMultiTask       是否为多任务节点
     * @return 是否需要初始化投票变量
     */
    private boolean checkNeedClearVotes(String taskId, String taskDefinitionKey, boolean isMultiTask) {
        boolean needClearVotes = true;
        if (isMultiTask) { // 多任务
            Integer nrOfCompletedInstances = TaskServiceUtil.queryCompletedTaskCountByTaskId(taskId);
            if (nrOfCompletedInstances != null && nrOfCompletedInstances > 0) { // 如果节点上有人已经在审，直接可以知道不需要清零
                return false;
            } else {
                // 循环获取该节点上各审批结果的统计票数，用来判断是否需要进行清零操作
                List<String> codeList = getHisOptionList(taskId);
                for (String actionCode : codeList) {
                    // 获取节点上的票数
                    Integer count = getCount(taskId, taskDefinitionKey, actionCode);
                    // 第一次进入这个节点，需要清零
                    if (count == null && nrOfCompletedInstances == 0) {
                        return true;
                        // 一旦判断出是第二次进到这个节点，则判断出可以直接清零
                    } else if (count != null && nrOfCompletedInstances != null && count > 0 && nrOfCompletedInstances == 0) {
                        return true;
                    }
                }
                needClearVotes = false;
            }
        }
        return needClearVotes;
    }

    @Override
    public Map<String, Integer> queryLastRunnedNodeVoteCount(String processInstanceId) throws UnitedException {
        List<HistoricTaskInstance> htiList = ServiceUtil.getHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .desc()
                .list();
        HistoricTaskInstance lastTaskInstance = htiList.get(0);
        return getAllCountByTaskDefinitionKey(lastTaskInstance.getProcessInstanceId(),
                lastTaskInstance.getTaskDefinitionKey());
    }

    @Override
    public String queryLastRunnedNodeId(String processInstanceId) throws Exception {
        List<HistoricTaskInstance> htiList = ServiceUtil.getHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .desc()
                .list();
        HistoricTaskInstance lastTaskInstance = htiList.get(0);
        return lastTaskInstance.getTaskDefinitionKey();
    }

    @Override
    public Map<String, Integer> getAllCountByTaskDefinitionKey(String processInstanceId, String taskDefinitionKey) {
        // List<String> codeList = ActionEnum.getAllCode(); // 后续这个要改成从数据库查询对应节点上的审批结果设置
        List<String> codeList = getHisOptionList(processInstanceId, taskDefinitionKey);
        Map<String, Integer> resultMap = new HashMap<>();
        for (String actionCode : codeList) {
            Integer count = getHistoryCount(processInstanceId, taskDefinitionKey, actionCode);
            if (count == null) {
                count = 0;
            }
            resultMap.put(actionCode, count);
        }
        return resultMap;
    }

    @Override
    public void updateApproverBatch(String tenantId, String originalUser, String replacedUser) throws UnitedException {
        if (StringUtils.isBlank(tenantId)) {
            throw new UnitedException("租户ID不能为空！");
        }
        if (StringUtils.isBlank(originalUser)) {
            throw new UnitedException("要替换的审批人不能为空！");
        }
        if (StringUtils.isBlank(replacedUser)) {
            throw new UnitedException("替换的审批人不能为空！");
        }
        List<Task> taskEntities = ServiceUtil.getTaskQuery().tenantIdIn(tenantId).taskAssignee(originalUser).list();
        taskEntities.forEach(t -> taskService.setAssignee(t.getId(), replacedUser));
    }

    @Override
    public void updateApprover(String taskId, String userId) throws UnitedException {
        if (StringUtils.isBlank(taskId)) {
            throw new UnitedException("任务ID不能为空！");
        }
        if (StringUtils.isBlank(userId)) {
            throw new UnitedException("审批人ID不能为空！");
        }
        taskService.setAssignee(taskId, userId);
    }

    /**
     * 初始化节点的总待办数、已完成待办数、各个选项的审批票数等
     *
     * @param taskId            待办ID
     * @param taskDefinitionKey 待办Key
     * @param isMultiTask       是否是多人审批节点
     * @param codeList          节点审批选项列表
     */
    private void clearVotes(String taskId, String taskDefinitionKey, boolean isMultiTask, List<String> codeList) {
        Map<String, Object> initVoteMap = new HashMap<>(16);
        String formatStr = taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_S_COUNT);
        // 初始化节点各个审批选项的流程变量，如果该节点的配置审批选项为空，则不需要初始化
        if (codeList != null) {
            initVoteMap.putAll(codeList.stream().filter(StringUtils::isNotBlank)
                    .collect(Collectors.toMap(o -> String.format(formatStr, o.trim()), o -> 0)));
        }
        // 初始化节点的总待办数【到下次进入这个方法之前都不会修改】
        initVoteMap.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_NROFINSTANCES),
                isMultiTask ? TaskServiceUtil.queryTotalTaskCountByTaskId(taskId) : 1);

        // 初始化节点的已完成待办数为0
        initVoteMap.put(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_NROFCOMPLETEDINSTANCES), 0);
        taskService.setVariables(taskId, initVoteMap);
    }


    /**
     * 获取票数
     *
     * @param taskId            待办id
     * @param taskDefinitionKey 节点名称
     * @param actionCode        节点审批结果code
     * @return count
     */
    private Integer getCount(String taskId, String taskDefinitionKey, String actionCode) {
        return (Integer) taskService.getVariable(taskId, getCountVariableName(taskDefinitionKey, actionCode));
    }

    /**
     * 获取历史节点的票数
     *
     * @param processInstanceId 流程实例id
     * @param taskDefinitionKey 节点名称
     * @param actionCode        节点审批结果code
     * @return historyCount
     */
    private Integer getHistoryCount(String processInstanceId, String taskDefinitionKey, String actionCode) {
        HistoricVariableInstance hvi = ServiceUtil.getHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName(getCountVariableName(taskDefinitionKey, actionCode)).singleResult();
        return (Integer) hvi.getValue();
    }

    /**
     * 获取计票节点的变量名称
     *
     * @param taskDefinitionKey 计票节点名称
     * @param actionCode        节点上审批结果code
     * @return countVariableName
     */
    private String getCountVariableName(String taskDefinitionKey, String actionCode) {
        return taskDefinitionKey + getCountSuffix(actionCode);
    }

    /**
     * 获取统计票数的后缀
     *
     * @param actionCode 审批结果code（pass和not_pass）
     * @return countSuffix
     */
    private String getCountSuffix(String actionCode) {
        return "_" + actionCode + BpmnVariableConstant.SUFFIX_COUNT;
    }

    // 获取审批选项list
    private List<String> getHisOptionList(String taskId) {
        String approveOptionsStr = getHisOptionStr(taskId);
        return hisOptionStrToList(approveOptionsStr);
    }

    private List<String> getHisOptionList(String processInstanceId, String taskDefinitionKey) {
        String approveOptionsStr = getHisOptionStr(processInstanceId, taskDefinitionKey);
        return hisOptionStrToList(approveOptionsStr);
    }

    // 逗号分隔字符串转换为list
    private List<String> hisOptionStrToList(String approveOptionsStr) {
        List<String> optionList;
        if (StringUtils.isEmpty(approveOptionsStr)) {
            optionList = Collections.emptyList();
        } else {
            optionList = Arrays.asList(approveOptionsStr.split(","));
        }
        return optionList;
    }

    // 获取审批选项字符串
    private String getHisOptionStr(String taskId) {
        Task task = TaskServiceUtil.getTaskByTaskId(taskId);
        if (task == null) {
            throw new RuntimeException("task not found！");
        }
        return getHisOptionStr(task.getProcessInstanceId(), task.getTaskDefinitionKey());
    }

    private String getHisOptionStr(String processInstanceId, String taskDefinitionKey) {
        HistoricVariableInstanceQuery hiq = ServiceUtil.getHistoricVariableInstanceQuery().processInstanceId(processInstanceId);
        HistoricVariableInstance approveOptionsV = HistoryServiceUtil.getHisVarInstByQueryName(hiq, taskDefinitionKey + BpmnVariableConstant.SUFFIX_APPROVEOPTIONS);
        return approveOptionsV != null ? (String) approveOptionsV.getValue() : null;
    }
}
