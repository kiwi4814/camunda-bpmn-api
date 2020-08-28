
package me.corningrey.camunda.api.service.impl;

import cn.hutool.core.collection.CollUtil;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.service.ProcessOperService;
import me.corningrey.camunda.api.util.RepositoryServiceUtil;
import me.corningrey.camunda.api.util.RuntimeServiceUtil;
import me.corningrey.camunda.api.util.TaskServiceUtil;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProcessOperServiceImpl implements ProcessOperService {

    @Resource
    private ProcessOperHistoryService processOperHistoryService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void turnProcess(String taskId, String activityId, String operUser, String reason) throws Exception {
        // 结束所有运行的任务
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(findTaskById(taskId).getProcessInstanceId()).list();
        Map<String, Object> vars = new HashMap<>();

        // 插入流程转向的流程操作日志
        processOperHistoryService.insertTurnHistory(taskId, activityId, operUser, reason);

        turnTransition(taskId, activityId, vars, reason);
        for (Task task : tasks) {
            if (StringUtils.equals(task.getId(), taskId)) {
                continue;
            }
            vars = new HashMap<>();
            vars.put(task.getId() + BpmnVariableConstant.SUFFIX_COMMENT, "流程转向" + activityId + "节点");//审批意见变量设置
            vars.put(task.getId() + BpmnVariableConstant.SUFFIX_ACTION, "turn");//审批动作变量设置
            taskService.complete(task.getId(), vars);
            // 插入流程转向的待办操作日志（comment 审批被取消）
            processOperHistoryService.insertTaskHistory(taskId, ProcessOperEnum.TURN.getValue(), "", "", reason);
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void turnLine(String taskId, String sequenceId, String operUser, String reason) throws UnitedException {
        // 校验数据
        if (StringUtils.isEmpty(taskId)) {
            throw new UnitedException("流程转向失败：待办id为空！");
        } else if (StringUtils.isEmpty(sequenceId)) {
            throw new UnitedException("流程转向失败：连线id为空！");
        } else if (StringUtils.isEmpty(operUser)) {
            throw new UnitedException("流程转向失败：操作人为空！");
        }

        // 插入流程转向的流程操作日志
        processOperHistoryService.insertTurnLineHistory(taskId, sequenceId, operUser, reason);
        Task task = TaskServiceUtil.getTaskByTaskId(taskId);
        // 获取当前task所在的activityId
        String currentActivityId = task.getTaskDefinitionKey();

        RuntimeServiceUtil.turnLine(task.getProcessInstanceId(), sequenceId, currentActivityId);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void oneKeyFinish(String processInstanceId, String operUser, String reason) throws Exception {
        if (StringUtils.isBlank(processInstanceId)) { // 没有传递流程实例id
            throw new UnitedException("流程实例id没有传");
        }

        // 查询流程实例运行到的节点上的待办
        Task task = TaskServiceUtil.queryRunnedTaskByInstanceId(processInstanceId);
        // 根据流程实例id获取通往结束节点的连线id（可能会有多条连线）
        List<String> sequenceIdList = RepositoryServiceUtil.queryToEndSequenceIdByInstanceId(processInstanceId);

        // 流程实例运行到的节点的taskId
        String taskId = task == null ? null : task.getId();
        if (StringUtils.isBlank(taskId)) { // 当前无待办
            throw new UnitedException("流程待办不存在");
        }
        if (CollUtil.isEmpty(sequenceIdList)) { // 当前流程没有结束节点
            throw new UnitedException("流程没有结束节点");
        }
        turnLine(taskId, sequenceIdList.get(0), operUser, reason);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void oneKeyFinish(List<String> processInstanceIdList, String operUser, String reason) throws Exception {
        if (CollUtil.isEmpty(processInstanceIdList)) { // 没有传递流程实例id
            throw new UnitedException("流程实例id没有传");
        }
        if (StringUtils.isBlank(operUser)) { // 当前无待办
            throw new UnitedException("操作人不存在");
        }

        for (String processInstanceId : processInstanceIdList) {
            oneKeyFinish(processInstanceId, operUser, reason);
        }
    }

    @Override
    public List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 存储当前节点所有流向临时变量
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        // 获取当前节点所有流向，存储到临时变量，然后清空
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        return oriPvmTransitionList;
    }

    @Override
    public ActivityImpl findActivitiImpl(String taskId, String activityId)
            throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);

        // 获取当前活动节点ID
        if (StringUtils.isBlank(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey();
        }

        // 根据流程定义，获取该流程实例的结束节点
        if ("END".equals(activityId.toUpperCase())) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl
                        .getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }

        // 根据节点ID，获取对应的活动节点
        ActivityImpl activityImpl = processDefinition.findActivity(activityId);

        return activityImpl;
    }

    @Override
    public void restoreTransition(ActivityImpl activityImpl,
                                  List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        for (PvmTransition pvmTransition : oriPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
    }

    @Override
    public void turnTransition(String taskId, String activityId,
                               Map<String, Object> variables, String reason) throws Exception {
        // 当前节点
        ActivityImpl currActivity = findActivitiImpl(taskId, null);
        // 清空当前流向
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);

        // 创建新流向
        TransitionImpl newTransition = currActivity.createOutgoingTransition();
        // 目标节点
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);
        // 设置新流向的目标节点
        newTransition.setDestination(pointActivity);

        // 执行转向任务
        variables.put(taskId + BpmnVariableConstant.SUFFIX_COMMENT, "流程转向" + activityId + "节点");//审批意见变量设置
        variables.put(taskId + BpmnVariableConstant.SUFFIX_ACTION, "turn");//审批动作变量设置
        taskService.complete(taskId, variables);

        // 插入流程转向的待办操作日志（comment 审批被取消）
        processOperHistoryService.insertTaskHistory(taskId, ProcessOperEnum.TURN.getValue(), "", "", reason);

        // 删除目标节点新流入
        pointActivity.getIncomingTransitions().remove(newTransition);

        // 还原以前流向
        restoreTransition(currActivity, oriPvmTransitionList);
    }

    @Override
    public ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(
            String taskId) throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId)
                        .getProcessDefinitionId());

        if (processDefinition == null) {
            throw new Exception("流程定义未找到!");
        }

        return processDefinition;
    }

    @Override
    public TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(
                taskId).singleResult();
        if (task == null) {
            throw new Exception("任务实例未找到!");
        }
        return task;
    }

    @Override
    public void deleteProcessInstanceByDefKey(String definitionKey) {
        // 根据流程定义key查找运行流程实例id
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(definitionKey).list();
        for (ProcessInstance pi : list) {
            runtimeService.deleteProcessInstance(pi.getProcessInstanceId(), "system delete");
        }
        // 根据流程定义key查找历史流程实例id
        List<HistoricProcessInstance> list1 = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(definitionKey).list();
        for (HistoricProcessInstance hpi : list1) {
            historyService.deleteHistoricProcessInstance(hpi.getId());
        }
    }

    @Override
    public void deleteProcessInstanceAndOperHis(String tenantId, String definitionKey) {
        // 删除流程实例
        deleteProcessInstanceByDefKey(definitionKey);
        // 删除操作日志
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        params.put("processDefinitionKey", definitionKey);
        processOperHistoryService.deleteProcessOperHis(params);
    }

    @Override
    public String queryBackProcessTaskId(String processInstanceId) {
        return processOperHistoryService.queryBackProcessTaskId(processInstanceId);
    }
}
