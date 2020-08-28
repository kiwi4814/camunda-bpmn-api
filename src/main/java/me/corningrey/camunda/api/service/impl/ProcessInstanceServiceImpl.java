
package me.corningrey.camunda.api.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import me.corningrey.camunda.api.dao.ProcessInstanceMapper;
import me.corningrey.camunda.api.dao.ProcessOperHistoryMapper;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.dom4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {


    @Resource
    private ProcessInstanceMapper processInstanceMapper;
    @Resource
    private ProcessOperHistoryMapper processOperHistoryMapper;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RepositoryService repositoryService;

    @Override
    public List<ActivityExt> findHistoricActivityList(ActivitySearch activitySearch) throws UnitedException {
        if (activitySearch == null || StringUtils.isBlank(activitySearch.getProcessInstanceId())) {
            throw new UnitedException("请传入流程实例ID！");
        }
        return processInstanceMapper.findHistoricActivityList(activitySearch);
    }

    @Override
    public Map<String, String> findProcessInstanceStatus(String instanceId, String taskId) throws UnitedException {
        if (StringUtils.isBlank(instanceId) && StringUtils.isBlank(taskId)) {
            throw new UnitedException("instanceId和taskId至少有一项不为空！");
        }
        Map<String, String> resultMap = processInstanceMapper.findProcessInstanceStatus(instanceId, taskId);
        if (resultMap == null) {
            throw new UnitedException("返回的数据为空！");
        }
        // 取出map中返回的数据
        String processInstanceId = resultMap.get("processInstanceId");
        String instanceState = resultMap.get("instanceState");
        String currentTaskDefKeyNow = resultMap.get("currentTaskDefKey");
        String singleKey = resultMap.get("singleKey");
        // 如果流程不是激活状态，直接返回
        if (!StringUtils.equals(CommonConstant.INST_STATE_ACTIVE, instanceState)) {
            resultMap.put("isUpdate", "1");
        } else {
            // 流程在激活状态下，通过当前的流程节点的key值来判断节点是否有更新
            Object o = runtimeService.getVariable(processInstanceId, BpmnVariableConstant.CURRENT_TASK_DEF_KEY);
            String currentTaskDefKey = Optional.ofNullable(o).map(Object::toString).orElse("");
            if (StringUtils.equals(currentTaskDefKeyNow, currentTaskDefKey)) {
                dealWithFinalApprover(processInstanceId, singleKey, false);
            } else {
                resultMap.put("isUpdate", "1");
                dealWithFinalApprover(processInstanceId, singleKey, true);
                runtimeService.setVariable(processInstanceId, BpmnVariableConstant.CURRENT_TASK_DEF_KEY, currentTaskDefKeyNow);
            }
        }
        return resultMap;
    }

    /**
     * 如果节点未更新，判断是否为最后一个审批人了，如果是就新增相关流程变量，否则不做改动
     */
    private void dealWithFinalApprover(String processInstanceId, String singleKey, boolean flag) {
        if (StringUtils.isNotBlank(singleKey)) {
            String isFinalApprover = singleKey.concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER);
            if (flag) {
                runtimeService.removeVariable(processInstanceId, isFinalApprover);
                return;
            }
            String instances = singleKey.concat(BpmnVariableConstant.SUFFIX_NROFINSTANCES);
            String completedInstances = singleKey.concat(BpmnVariableConstant.SUFFIX_NROFCOMPLETEDINSTANCES);
            Map<String, Object> variableMap = runtimeService.getVariables(processInstanceId, Arrays.asList(instances, completedInstances));
            Integer instanceCount = (Integer) variableMap.get(instances);
            Integer completedInstanceCount = (Integer) variableMap.get(completedInstances);
            if (instanceCount != null && completedInstanceCount != null) {
                if (instanceCount - completedInstanceCount == 1) {
                    runtimeService.setVariable(processInstanceId, isFinalApprover, "1");
                }
            }
        }
    }

    @Override
    public List<String> findInstanceIdsByAssignee(String tenantId, String assignee) {
        List<String> assigneeList = Arrays.asList(assignee.split(","));
        if (StringUtils.isBlank(tenantId)) {
            throw new IllegalArgumentException("租户id没有传");
        }
        if (CollUtil.isEmpty(assigneeList)) {
            throw new IllegalArgumentException("审批人id没有传");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("tenantId", tenantId);
        map.put("assignee", assigneeList);
        return processInstanceMapper.findInstanceIdsByAssignee(map);
    }

    /**
     * 流程跳转
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void executeProcessModification(String instanceId, String activityId, String variableMap, String isAfter, String operUser, String operType, String reason) throws Exception {
        String processDefinitionId = findProcessDefinitionByInstance(instanceId);
        if (StringUtils.isBlank(processDefinitionId)) {
            throw new UnitedException("流程已结束，无法执行跳转");
        }
        ProcessInstanceModificationBuilder modificationBuilder = runtimeService.createProcessInstanceModification(instanceId);
        // 获取真正的跳转目标
        Map<String, String> resultMap = new HashMap<>(8);
        resultMap.put("targetActivityId", activityId);
        parseRealModificationTarget(processDefinitionId, resultMap);
        String realTarget = resultMap.get("targetActivityId");
        String isSequenceFlow = resultMap.get("isSequenceFlow");
        // 日志部分
        if (StrUtil.isBlank(operType)) {
            operType = ProcessOperEnum.TURN.getValue();
        }
        Map<String, String> operMap = processInstanceMapper.findActiveActivity(instanceId);
        if (operMap != null) {
            resultMap.putAll(operMap);
        }
        // 取消当前正在激活的所有任务
        ActivityInstance activityInstance = runtimeService.getActivityInstance(instanceId);
        if (activityInstance != null) {
            for (ActivityInstance childActivityInstance : activityInstance.getChildActivityInstances()) {
                modificationBuilder.cancelAllForActivity(childActivityInstance.getActivityId());
            }
        }
        // 执行跳转
        Map<String, Object> map = CamundaUtil.convertJsonStrToMap(variableMap);
        if (StrUtil.isNotBlank(isSequenceFlow)) {
            modificationBuilder.startTransition(realTarget).setVariables(map).execute();
        } else if (StrUtil.isNotBlank(isAfter)) {
            modificationBuilder.startAfterActivity(realTarget).setVariables(map).execute();
        } else {
            modificationBuilder.startBeforeActivity(realTarget).setVariables(map).execute();
        }
        // 保存操作日志
        insertProcessHistory(instanceId, operUser, operType, reason, resultMap);

    }

    /**
     * 流程重启
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void activeAndModifyToStart(String instanceId, String variableMap, String operUser, String operType, String reason) throws Exception {
        // 开始执行
        Map<String, String> base = processInstanceMapper.findInstanceBaseInfoById(instanceId);
        String status = base.get("STATUS");
        if (StringUtils.isBlank(status) || StrUtil.equals(status, "1")) {
            throw new UnitedException("要恢复的流程已经结束或者不存在！");
        }
        String state = base.get("STATE");
        if (StrUtil.equals(state, CommonConstant.INST_STATE_SUSPENDED)) {
            runtimeService.activateProcessInstanceById(instanceId);
        }
        // 日志部分
        if (StrUtil.isBlank(operType)) {
            operType = ProcessOperEnum.ACTIVATE.getValue();
        }
        Map<String, String> operMap = processInstanceMapper.findActiveActivity(instanceId);
        // 在流程未调用runtimeService.restartProcessInstances时，开始节点可以从表里取值（暂时未开放Restart方法）
        String startActivityId = base.get("START");
        ProcessInstanceModificationBuilder modificationBuilder = runtimeService.createProcessInstanceModification(instanceId);
        // 取消当前正在激活的所有任务
        ActivityInstance activityInstance = runtimeService.getActivityInstance(instanceId);
        if (activityInstance != null) {
            for (ActivityInstance childActivityInstance : activityInstance.getChildActivityInstances()) {
                modificationBuilder.cancelAllForActivity(childActivityInstance.getActivityId());
            }
        }
        // 执行跳转
        Map<String, Object> map = CamundaUtil.convertJsonStrToMap(variableMap);
        modificationBuilder.startAfterActivity(startActivityId).setVariables(map).execute();
        // 保存操作日志
        insertProcessHistory(instanceId, operUser, operType, reason, operMap);
    }

    @Override
    public String findProcessDefinitionByInstance(String instanceId) {
        return processInstanceMapper.findProcessDefinitionByInstance(instanceId);
    }

    /**
     * 根据activityId查找节点类型等
     *
     * @param processDefinitionId 流程定义ID
     * @throws DocumentException
     */
    private void parseRealModificationTarget(String processDefinitionId, Map<String, String> resultMap) throws DocumentException {
        final String MULTI_SUFFIX = "#multiInstanceBody";
        String activityId = resultMap.get("targetActivityId");
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        Document document = DocumentHelper.parseText(Bpmn.convertToString(bpmnModelInstance));
        activityId = StrUtil.removeSuffix(activityId, MULTI_SUFFIX);
        String xpath = "/bpmn:definitions/bpmn:process/*[@id='" + activityId + "']";
        Element node = (Element) document.selectSingleNode(xpath);
        if (node != null) {
            String elementName = node.getName();
            String actName = node.attributeValue("name");
            resultMap.put("targetActivityName", actName);
            if (StrUtil.equals("sequenceFlow", elementName)) {
                resultMap.put("targetActivityId", activityId);
                resultMap.put("isSequenceFlow", "1");
                return;
            }
            if (StrUtil.endWith(elementName, "Gateway") || StrUtil.equalsAny(elementName, "endEvent", "startEvent")) {
                resultMap.put("targetActivityId", activityId);
            } else {
                List<Node> nodeList = document.selectNodes(xpath + "/bpmn:multiInstanceLoopCharacteristics");
                if (nodeList != null && nodeList.size() > 0) {
                    resultMap.put("targetActivityId", activityId.concat(MULTI_SUFFIX));
                }
            }
        }
    }

    private void insertProcessHistory(String instanceId, String operUser, String operType, String reason, Map<String, String> operComment) {
        ProcessOperHistory p = ProcessOperHistory.builder()
                .processInstanceId(instanceId)
                .operType(operType)
                .operUser(operUser)
                .operComment(JSON.toJSONString(operComment))
                .operReason(reason)
                .build();
        processOperHistoryMapper.insert(p);
    }
}
