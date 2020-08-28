package me.corningrey.camunda.api.cmd;

import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.model.SignTask;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ManualSignedCmd implements Command<List<String>> {
    /**
     * 加签的待办
     */
    protected String taskId;
    /**
     * 加签说明
     */
    private String reason;
    /**
     * 加签用户
     */
    private List<String> userList;

    private String operUser;

    private TaskAPIMapper taskAPIMapper;


    public ManualSignedCmd(TaskAPIMapper taskAPIMapper, String taskId, List<String> userList, String reason, String operUser) {
        this.taskId = taskId;
        this.reason = reason;
        this.operUser = operUser;
        this.userList = userList;
        this.taskAPIMapper = taskAPIMapper;

    }


    @Override
    public List<String> execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);
        if (taskEntity == null) {
            throw new RuntimeException("当前任务不存在或者已完成！");
        }
        // 初始化
        SignTask signTask = new SignTask();
        List<String> taskIds = new LinkedList<>();
        String taskDefinitionKey = taskEntity.getTaskDefinitionKey();
        int nrOfInstances = updateProcessVariable(taskEntity, taskDefinitionKey);
        signTask.setTaskId(taskId);
        signTask.setExecutionId(taskEntity.getExecutionId());
        signTask.setActivityId(taskEntity.getExecution().getActivityInstanceId());

        // 循环增加待办
        IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
        for (int i = 0; i < userList.size(); i++) {
            String signTaskId = "SIGN_".concat(idGenerator.getNextId());
            signTask.setSignExecutionId(idGenerator.getNextId());
            signTask.setSignTaskId(signTaskId);
            signTask.setSignActivityId(taskDefinitionKey.concat(":").concat(idGenerator.getNextId()));
            signTask.setSignUser(userList.get(i));
            // 手动新增数据
            taskAPIMapper.addSignExecutionEntity(signTask);
            taskAPIMapper.addSignActivityEntity(signTask);
            taskAPIMapper.addSignTaskEntity(signTask);
            taskAPIMapper.addSignTaskHisEntity(signTask);
            // 新增user和lppCounter实例流程变量
            signTask.setVariableId(idGenerator.getNextId());
            signTask.setKey("user");
            signTask.setTextValue(userList.get(i));
            taskAPIMapper.copySignVariable(signTask);
            taskAPIMapper.copySignVariableHis(signTask);
            signTask.setVariableId(idGenerator.getNextId());
            signTask.setKey("loopCounter");
            signTask.setLongValue(nrOfInstances + i);
            signTask.setTextValue(String.valueOf(nrOfInstances + i));
            taskAPIMapper.copySignVariable(signTask);
            taskAPIMapper.copySignVariableHis(signTask);
            // 插入历史记录
            JSONObject json = new JSONObject();
            if (StringUtils.isBlank(operUser)) {
                operUser = taskEntity.getAssignee();
            }
            json.put("signUsers", userList.stream().collect(Collectors.joining(",")));
            json.put("reason", reason);
            taskAPIMapper.insertProcessOperHistory(
                    ProcessOperHistory.builder()
                            .processInstanceId(taskEntity.getProcessInstanceId())
                            .taskId(taskId)
                            .operType(ProcessOperEnum.APPEND.getValue())
                            .operUser(operUser).operReason(reason).operComment(json.toJSONString()).build()
            );
            taskIds.add(signTaskId);
        }
        return taskIds;
    }


    private int updateProcessVariable(TaskEntity taskEntity, String taskDefinitionKey) {
        // 获取当前Task的Execution执行实例
        ExecutionEntity executionEntity = taskEntity.getExecution();
        // 获取当前Task的父级Execution执行实例，如果是子流程，需要获取两次（这里暂时不考虑子流程）
        ExecutionEntity executionEntityParent = executionEntity.getParent();
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
        return nrOfInstances;
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
