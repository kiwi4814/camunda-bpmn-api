
package me.corningrey.camunda.api.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.dao.ProcessOperHistoryMapper;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.util.CommonUtil;
import me.corningrey.camunda.api.util.HistoryServiceUtil;
import me.corningrey.camunda.api.util.RepositoryServiceUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class ProcessOperHistoryServiceImpl implements ProcessOperHistoryService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private ProcessOperHistoryMapper processOperHistoryMapper;

    @Override
    public void insertBackHistory(String taskId, String reason, String suspendStr) {
        String operType = "";
        if (suspendStr.equals(CommonConstant.SUSPEND_TRUE)) {
            operType = ProcessOperEnum.SUSPEND.getValue();
        } else if (suspendStr.equals(CommonConstant.SUSPEND_FALSE)) {
            operType = ProcessOperEnum.ACTIVATE.getValue();
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 操作说明（reason）
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reason", reason);
        insertProcessOperHistory(task.getProcessInstanceId(), taskId, operType, task.getAssignee(), jsonObject.toString(), reason);
    }

    @Override
    public void insertCancelHistory(String processInstanceId, String operUser, String reason) {
        // 操作说明（reason）
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reason", reason);
        insertProcessOperHistory(processInstanceId, "", ProcessOperEnum.CANCEL.getValue(), operUser, jsonObject.toString(), reason);
    }

    @Override
    public void insertTurnHistory(String taskId, String activityId, String operUser, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        // 操作说明（"流程从" + taskId + "转向" + activityId + "节点"）
        String startTaskDefinitionName = task.getName(); // 起始节点名称

        Map<String, Object> endTask = RepositoryServiceUtil.queryElementSimpleInfoById(task.getProcessDefinitionId(), activityId);
        String endTaskDefinitionName = (String) endTask.get("taskDefinitionName"); // 目标节点名称

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskId", taskId);
        jsonObject.put("activityId", activityId); // 目标节点id
        jsonObject.put("startNodeName", startTaskDefinitionName); // 起始节点名称
        jsonObject.put("endNodeName", endTaskDefinitionName); // 目标节点名称
        insertProcessOperHistory(task.getProcessInstanceId(), taskId, ProcessOperEnum.TURN.getValue(), operUser,
                jsonObject.toJSONString(), reason);
    }

    @Override
    public void insertTurnLineHistory(String taskId, String sequenceId, String operUser, String reason) throws UnitedException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new UnitedException("操作日志插入失败：待办id为空！");
        }

        // 操作说明（"流程从" + taskId + "转向" + activityId + "节点"）
        String startTaskDefinitionName = task.getName(); // 起始节点名称

        Map<String, Object> sequenceFlow = RepositoryServiceUtil.queryElementSimpleInfoById(task.getProcessDefinitionId(), sequenceId);
        String endTaskDefinitionName = (String) sequenceFlow.get("endNodeName"); // 连线指向的目标节点名称

        // 获取目标节点
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskId", taskId);
        jsonObject.put("sequenceId", sequenceId); // 连线id
        jsonObject.put("startNodeName", startTaskDefinitionName); // 起始节点名称
        jsonObject.put("endNodeName", endTaskDefinitionName); // 连线指向的目标节点名称
        insertProcessOperHistory(task.getProcessInstanceId(), taskId, ProcessOperEnum.TURN.getValue(), operUser,
                jsonObject.toJSONString(), reason);
    }

    @Override
    public void insertTaskHistory(String taskId, String operType, String operUser, String operComment, String reason) {
        HistoricTaskInstance hti = HistoryServiceUtil.getHistoricTaskInstanceByTaskId(taskId);
        // Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        insertProcessOperHistory(hti.getProcessInstanceId(), taskId, operType, operUser, operComment, reason);
    }

    @Override
    public void insertProcessOperHistory(ProcessOperHistory processOperHistory) {
        processOperHistoryMapper.insert(processOperHistory);
    }

    @Override
    public void insertProcessOperHistory(String processInstanceId, String taskId, String operType, String operUser, String operComment, String reason) {
        ProcessOperHistory p = ProcessOperHistory.builder()
                .processInstanceId(processInstanceId)
                .taskId(taskId)
                .operType(operType)
                .operUser(operUser)
                .operComment(operComment)
                .operReason(reason)
                .build();
        insertProcessOperHistory(p);
    }

    @Override
    public List<ProcessOperHistory> findProcessOperHistory(OperHistorySearch operHistorySearch) throws UnitedException {
        Map<String, Object> variables = CamundaUtil.convertJsonStrToMap(operHistorySearch.getVariables());
        String operTypes = CamundaUtil.decode(operHistorySearch.getOperTypes(), StandardCharsets.UTF_8.name());
        List<ProcessOperHistory> processOperHistoryList = processOperHistoryMapper.findByProcessInstanceId(operHistorySearch.getTenantId(),
                operHistorySearch.getProcessInstanceId(), operHistorySearch.getTaskDefinitionKey(), variables, operTypes);

        if ("1".equals(operHistorySearch.getOperStatus())) {
            if (StringUtils.isNotBlank(operHistorySearch.getProcessInstanceId()) ||
                    CommonUtil.isJSONValid(operHistorySearch.getVariables())) {
                processOperHistoryList.addAll(getUnfinishedTaskByInst(operHistorySearch));
            }
        }
        return processOperHistoryList;
    }

    // 获取正在运行的流程实例的未完成待办信息
    private List<ProcessOperHistory> getUnfinishedTaskByInst(OperHistorySearch operHistorySearch) {
        List<ProcessOperHistory> resultList = new ArrayList<>();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

        // 如果processInstanceId不为空
        if (StringUtils.isNotBlank(operHistorySearch.getProcessInstanceId())) {
            query.processInstanceId(operHistorySearch.getProcessInstanceId());
        }

        // 如果流程变量不为空
        if (CommonUtil.isJSONValid(operHistorySearch.getVariables())) {
            Map<String, Object> variables = JSON.parseObject(operHistorySearch.getVariables(), Map.class);
            for (Map.Entry entry : variables.entrySet()) {
                query.processVariableValueEquals((String) entry.getKey(), entry.getValue());
            }
        }
        if (StringUtils.isNotBlank(operHistorySearch.getTenantId())) {
            query.tenantIdIn(operHistorySearch.getTenantId());
        }
        List<HistoricTaskInstance> tasks = query.unfinished().list();
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(t -> resultList.add(getOperHistoryByTask(t)));
        }
        return resultList;
    }

    public ProcessOperHistory getOperHistoryByTask(HistoricTaskInstance hti) {
        // 获取流程实例名称
        String processInstanceRemark = (String) runtimeService.getVariable(hti.getExecutionId(), BpmnVariableConstant.INST_REMARK);
        // 获取流程定义名称
        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition pd = pdq.processDefinitionId(hti.getProcessDefinitionId()).singleResult();
        return ProcessOperHistory.builder()
                .operUser(hti.getAssignee())
                .processInstanceId(hti.getProcessInstanceId())
                .processName(pd.getName())
                .processInstanceRemark(processInstanceRemark)
                .nodeName(hti.getName())
                .taskId(hti.getId())
                .build();
    }

    @Override
    public void deleteProcessOperHis(Map<String, String> params) {
        processOperHistoryMapper.deleteProcessOperHis(params);
    }

    @Override
    public String queryBackProcessTaskId(String processInstanceId) {
        return processOperHistoryMapper.queryBackProcessTaskId(processInstanceId);
    }
}
