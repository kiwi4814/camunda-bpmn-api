package me.corningrey.camunda.api.cmd;

import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import org.apache.commons.lang3.RandomStringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.util.LinkedList;
import java.util.List;

public class AddSignedTaskCmd implements Command<List<String>> {
    // 待办ID
    protected String taskId;
    // 要加签的用户集合
    private List<String> userList;


    public AddSignedTaskCmd(String taskId, List<String> userList) {
        this.taskId = taskId;
        this.userList = userList;
    }


    @Override
    public List<String> execute(CommandContext commandContext) {
        TaskService taskService = commandContext.getProcessEngineConfiguration().getTaskService();
        // 获取当前任务实例
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);
        if (taskEntity == null) {
            throw new RuntimeException("当前任务不存在或者已完成！");
        }
        List<String> taskIds = new LinkedList<>();
        String processDefinitionId = taskEntity.getProcessDefinitionId();
        String processInstanceId = taskEntity.getProcessInstanceId();
        UnitedLogger.error("processInstanceId：" + processInstanceId);
        String taskDefinitionKey = taskEntity.getTaskDefinitionKey();
        // 获取当前Task的Execution执行实例
        ExecutionEntity executionEntity = taskEntity.getExecution();
        UnitedLogger.error("CurrentTask-Execution：" + executionEntity.getId());
        // 获取当前Task的父级Execution执行实例，如果是子流程，需要获取两次（这里暂时不考虑子流程）
        ExecutionEntity executionEntityParent = executionEntity.getParent();
        UnitedLogger.error("ParentTask-Execution：" + executionEntityParent.getId());
        // 获取当前Task的Activity活动实例
        ActivityImpl activity = executionEntity.getActivity();
        // 获取当前Task的父级Activity活动实例
        ActivityImpl activityParent = activity.getParentFlowScopeActivity();
        // 更新父级Execution的计数范围变量
        int signCount = userList.size();
        int nrOfInstances = getLoopVariable(executionEntity, "nrOfInstances");
        int nrOfActiveInstances = getLoopVariable(executionEntity, "nrOfActiveInstances");
        executionEntityParent.setVariableLocal("nrOfInstances", nrOfInstances + signCount);
        executionEntityParent.setVariableLocal("nrOfActiveInstances", nrOfActiveInstances + signCount);
        // 更新流程实例的计数流程变量
        String taskNrOfInstancesKey = taskDefinitionKey.concat("_nrOfInstances");
        Integer taskNrOfInstancesValue = (Integer) executionEntity.getVariable(taskNrOfInstancesKey);
        if (taskNrOfInstancesValue != null) {
            executionEntity.setVariable(taskNrOfInstancesKey, taskNrOfInstancesValue + signCount);
        }
        // 移除已存在的_isFinalApprover变量
        if (signCount > 0) {
            executionEntity.removeVariable(taskDefinitionKey.concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER));
        }
        // 获取ID生成器并循环生成任务
        IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
        for (int i = 0; i < userList.size(); i++) {
            // 创建新的ActivityImpl用于创建ExecutionEntity
            ActivityImpl newActivity = activityParent.createActivity(taskDefinitionKey + RandomStringUtils.random(5, true, true));
            newActivity.setId(activity.getActivityId());
            newActivity.setName(activity.getName());
            newActivity.setProperties(activity.getProperties());
            newActivity.setActivityBehavior(activity.getActivityBehavior());
            newActivity.setDelegateAsyncAfterUpdate(activity.getDelegateAsyncAfterUpdate());
            newActivity.setDelegateAsyncBeforeUpdate(activity.getDelegateAsyncBeforeUpdate());
            // 创建新的ExecutionEntity用于创建任务
            ExecutionEntity executionEntityNew = executionEntityParent.createExecution();
            executionEntityNew.setActivity(newActivity);
            // executionEntityNew.setActivityInstanceId();
            executionEntityNew.setActive(true);
            executionEntityNew.setConcurrent(true);
            executionEntityNew.setScope(false);
            executionEntityNew.setRevision(0);
            executionEntityNew.setCachedEntityState(executionEntity.getCachedEntityState());
            executionEntityNew.setSequenceCounter(executionEntity.getSequenceCounter());
            // 新增此ExecutionEntity执行实例的范围变量
            executionEntityNew.setVariableLocal("loopCounter", nrOfInstances + i);
            executionEntityNew.setVariableLocal("user", userList.get(i));

            // 创建新的任务
            TaskEntity taskNew = new TaskEntity();
            taskNew.setId("SIGN_" + idGenerator.getNextId());
            taskNew.setTaskDefinitionKey(taskDefinitionKey);
            taskNew.setName(taskEntity.getName());
            taskNew.setTenantId(taskEntity.getTenantId());
            // taskNew.setCreateTime(new Date());
            taskNew.setProcessDefinitionId(processDefinitionId);
            taskNew.setProcessInstance(taskEntity.getProcessInstance());
            taskNew.setProcessInstanceId(processInstanceId);
            taskNew.setExecutionId(executionEntityNew.getId());
            taskNew.setExecution(executionEntityNew);
            taskNew.setRevision(0);
            taskNew.setAssignee(userList.get(i));
            taskService.saveTask(taskNew);
            taskIds.add(taskNew.getId());
        }
        return taskIds;
    }


    private Integer getLoopVariable(ExecutionEntity execution, String variableName) {
        Object value = execution.getVariableLocal(variableName);
        DelegateExecution parent = execution.getParent();
        while (value == null && parent != null) {
            value = parent.getVariableLocal(variableName);
        }
        return (Integer) (value != null ? value : 0);
    }

}
