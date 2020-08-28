package me.corningrey.camunda.api.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/instance/")
@Api(value = "InstanceController", tags = {"「03」 流程实例相关接口"})
public class InstanceController {
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private IdentityService identityService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ProcessInstanceService processInstanceService;
    @Resource
    private ProcessOperHistoryService processOperHistoryService;

    @ApiOperation(value = "3.1 发起一个流程实例", position = 1)
    @PostMapping(value = "/startProcessInstance.json")
    // @ApiOperationSupport(order = 1)
    public Result startProcessInstance(@ModelAttribute("processStart") ProcessStart processStart) {
        Result result = new Result();
        String proKey = processStart.getProcessDefinitionKey();
        String proId = processStart.getProcessDefinitionId();
        try {
            ProcessInstance instance;
            Map<String, Object> variables = CamundaUtil.convertJsonStrToMap(processStart.getVariables());
            variables.put(BpmnVariableConstant.INST_REMARK, processStart.getInstanceRemark());
            variables.put(BpmnVariableConstant.OPTIONAL_USERS, processStart.getOptionalUsers());
            /*// 处理自定义属性
            Map<String, Object> customExtensionMap = bpmnSettingService.selectCustomExtensionList(proKey, proId);
            if (customExtensionMap != null && !customExtensionMap.isEmpty()) {
                variables.putAll(customExtensionMap);
            }
            // 查询表单配置的参数并校验数据类型
            TenancyForm tenancyForm = tenancyFormService.selectByProcessDesign(proKey, proId);
            if (tenancyForm != null) {
                List<FormVariable> formVariableList = CamundaUtil.union(tenancyForm.getFormVariableList(), tenancyForm.getCustomVariableList(), tenancyForm.getApproverVariableList());
                // 这里要注意,只有配置了表单的情况下才会去校验流程类型，也就是自定义的参数只支持String
                CamundaUtil.resetVarType(variables, formVariableList);
            }*/
            identityService.setAuthenticatedUserId(processStart.getApplicant());
            if (StringUtils.isNotBlank(proId)) {
                instance = runtimeService.startProcessInstanceById(proId, processStart.getBusinessKey(), variables);
            } else if (StringUtils.isNotBlank(proKey)) {
                instance = runtimeService.startProcessInstanceByKey(proKey, processStart.getBusinessKey(), variables);
            } else {
                throw new UnitedException("发起流程失败！参数至少要有processDefinitionId和processDefinitionKey的一种");
            }
            if (instance != null) {
                // 【流程实例日志】流程发起
                processOperHistoryService.insertProcessOperHistory(
                        ProcessOperHistory.builder()
                                .processInstanceId(instance.getId())
                                .operType(ProcessOperEnum.START.getValue())
                                .operUser(processStart.getApplicant()).build()
                );
                // 待办审批完成之后，返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
                Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(instance.getProcessInstanceId(), null);
                result.setData(resultMap);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            result.setMessage(CamundaUtil.getRealMessage(e));
        }
        return result;
    }


    @ApiOperation(value = "3.2 查询流程实例列表", position = 2)
    @GetMapping(value = "/findHisInstanceList.json")
    // @ApiOperationSupport(order = 2)
    public Result findHisInstanceList(@ModelAttribute("processSearch") ProcessSearch processSearch) {
        Result result = new Result();
        HistoricProcessInstanceQuery hisProInstQuery = getHistoricProcessInstanceQuery(processSearch);
        if (hisProInstQuery != null) {
            // 处理排序
            String orderString = processSearch.getOrderString();
            historicProcessInstanceQuerySort(hisProInstQuery, orderString);
            // 开始分页
            Map<String, Object> resultMap = CamundaUtil.getCamundaParamer(processSearch.getPageNum(),
                    processSearch.getPageLimit(), hisProInstQuery);
            List<HistoricProcessInstance> listHi = (List<HistoricProcessInstance>) resultMap.get("list");
            // 重新封装
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            List<ProcessInstanceExt> resultList = new ArrayList<>();
            // 流程自定义名称赋值
            listHi.forEach(t -> {
                ProcessInstanceExt p = mapperFactory.getMapperFacade().map(t, ProcessInstanceExt.class);
                HistoricVariableInstance ins = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(t.getId()).variableName(BpmnVariableConstant.INST_REMARK).singleResult();
                p.setInstanceRemark(Optional.ofNullable(ins).map(HistoricVariableInstance::getValue).map(Object::toString).orElse(p.getProcessDefinitionName()));
                resultList.add(p);
            });
            resultMap.put("list", resultList);
            result.setData(resultMap);
        } else {
            result.setData(new HashMap<>(0));
        }
        result.setResultCode(ResultEnum.SUCCESS.getValue());
        return result;
    }

    @ApiOperation(value = "3.3 查询单个流程实例详情", position = 3)
    @GetMapping(value = "/findInstanceInfo.json")
    // @ApiOperationSupport(order = 3)
    @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true)
    public Result findInstanceInfo(@RequestParam("processInstanceId") String instanceId) {
        Result result = new Result();
        JSONObject jsonObject = new JSONObject();
        String processXml;
        List<ActivityExt> hisActivity;
        try {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            if (instance != null) {
                // 查询流程实例对象
                MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
                ProcessInstanceExt p = mapperFactory.getMapperFacade().map(instance, ProcessInstanceExt.class);
                HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(instanceId);
                List<HistoricVariableInstance> variableList = variableQuery.variableTypeIn("string,integer".split(",")).list();
                p.setVariableList(mapperFactory.getMapperFacade().mapAsList(variableList, VariableExt.class));
                HistoricVariableInstance ins = variableQuery.variableName(BpmnVariableConstant.INST_REMARK).singleResult();
                p.setInstanceRemark(Optional.ofNullable(ins).map(HistoricVariableInstance::getValue).map(Object::toString).orElse(p.getProcessDefinitionName()));
                // 查询流程定义XML
                BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(instance.getProcessDefinitionId());
                processXml = Bpmn.convertToString(bpmnModelInstance);
                // 查询流程activity列表
                hisActivity = processInstanceService.findHistoricActivityList(ActivitySearch.builder().processInstanceId(instanceId).build());
                jsonObject.put("processInstance", p);
                jsonObject.put("processXml", processXml);
                jsonObject.put("hisActivityList", hisActivity);
                result.setData(jsonObject);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            } else {
                result.setResultCode(ResultEnum.ERROR.getValue());
                result.setMessage("can not find any process instance");
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "3.4 根据实例ID/待办ID查询流程变量", position = 4)
    // @ApiOperationSupport(order = 4)
    @GetMapping(value = "/findProcessVariables.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "流程实例ID"),
            @ApiImplicitParam(name = "taskId", value = "任务ID"),
            @ApiImplicitParam(name = "variableKeys", value = "变量名称", required = true)
    })
    public Result findProcessVariables(@RequestParam(value = "instanceId", defaultValue = "") String instanceId,
                                       @RequestParam(value = "taskId", defaultValue = "") String taskId,
                                       @RequestParam("variableKeys") String variableKeys) {
        Result result = new Result();
        Map<String, Object> variableMap = new HashMap<>();
        try {
            final HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery();
            if (StringUtils.isNotBlank(instanceId)) {
                variableQuery.processInstanceId(instanceId);
            } else if (StringUtils.isNotBlank(taskId)) {
                HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
                variableQuery.processInstanceId(task.getProcessInstanceId());
            }
            variableKeys = CamundaUtil.decode(variableKeys, StandardCharsets.UTF_8.name());
            List<String> variableList = Arrays.asList(StringUtils.split(variableKeys, ","));
            variableList.forEach(k -> {
                HistoricVariableInstance v = variableQuery.variableName(k).singleResult();
                variableMap.put(k, Optional.ofNullable(v).map(HistoricVariableInstance::getValue).orElse(""));
            });
            result.setData(variableMap);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }


    @ApiOperation(value = "3.5 返回流程实例已经走过的节点及线的信息", position = 5)
    // @ApiOperationSupport(order = 5)
    @GetMapping(value = "/findActivityList.json")
    @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true)
    public Result findActivityList(@RequestParam("processInstanceId") String instanceId, String actTypes) {
        Result result = new Result();
        List<ActivityExt> hisActivity;
        try {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            if (instance != null) {
                // 查询流程activity列表
                hisActivity = processInstanceService.findHistoricActivityList(ActivitySearch.builder().processInstanceId(instanceId).actTypes(actTypes).build());
                BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(instance.getProcessDefinitionId());
                List<SequenceFlow> sequenceFlowList = (List<SequenceFlow>) modelInstance.getModelElementsByType(SequenceFlow.class);
                hisActivity.forEach(h -> sequenceFlowList.forEach(t -> {
                    if (StringUtils.equalsIgnoreCase(h.getActivityId(), t.getTarget().getId())) {
                        h.setIncoming(t.getId());
                    }
                }));
                result.setData(hisActivity);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            } else {
                result.setResultCode(ResultEnum.ERROR.getValue());
                result.setMessage("can not find any process instance");
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }


    @ApiOperation(value = "3.6 删除流程实例", position = 6)
    // @ApiOperationSupport(order = 6)
    @PostMapping(value = "/deleteProcessInstances.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "containRunningInstance", value = "是否删除正在运行中的实例"),
            @ApiImplicitParam(name = "isCompletelyDelete", value = "当删除运行中的实例时，是否同时删除所有的历史数据")
    })
    public Result deleteProcessInstances(@ModelAttribute("processSearch") ProcessSearch processSearch,
                                         @RequestParam(value = "containRunningInstance", defaultValue = "false") Boolean containRunningInstance,
                                         @RequestParam(value = "isCompletelyDelete", defaultValue = "false") Boolean isCompletelyDelete) {
        Result result = new Result();
        if (StringUtils.isBlank(processSearch.getTenancyId())) {
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage("请传入租户ID");
            return result;
        }
        try {
            HistoricProcessInstanceQuery hisProInstQuery = getHistoricProcessInstanceQuery(processSearch);
            int deleteCount = 0;
            Map<String, Object> resultMap = new HashMap<>();
            if (hisProInstQuery != null) {
                // 查询已完成的流程实例ID
                List<String> processInstanceIds = hisProInstQuery.list().stream()
                        .filter(t -> t.getEndTime() != null)
                        .map(HistoricProcessInstance::getId)
                        .collect(Collectors.toList());
                if (processInstanceIds.size() > 0) {
                    deleteCount += processInstanceIds.size();
                    historyService.deleteHistoricProcessInstances(processInstanceIds);
                }
                // 查询正在运行中的流程实例ID
                if (containRunningInstance) {
                    processInstanceIds = hisProInstQuery.list().stream()
                            .filter(t -> t.getEndTime() == null)
                            .map(HistoricProcessInstance::getId)
                            .collect(Collectors.toList());
                    if (processInstanceIds.size() > 0) {
                        runtimeService.deleteProcessInstances(processInstanceIds, "ARTIFICIAL_DELETION", true, true, true);
                        if (isCompletelyDelete) {
                            deleteCount += processInstanceIds.size();
                            historyService.deleteHistoricProcessInstances(processInstanceIds);
                        }
                    }
                }
            }
            resultMap.put("deleteCount", deleteCount);
            result.setData(resultMap);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private HistoricProcessInstanceQuery getHistoricProcessInstanceQuery(ProcessSearch processSearch) {
        // **********************history 实例查询**********************（这其中包括runtime中查询出来的所有实例）
        HistoricProcessInstanceQuery hisProInstQuery = historyService.createHistoricProcessInstanceQuery();
        // 查询条件：assignee（支持传多个assignee，逗号分隔）【首先判断此条件，因为如果此条件查出来的列表为空，就无需继续了】
        String assignees = processSearch.getAssignee();
        if (StringUtils.isNotBlank(assignees)) {
            assignees = CamundaUtil.decode(assignees, StandardCharsets.UTF_8.name());
            List<String> instanceIdList = processInstanceService.findInstanceIdsByAssignee(processSearch.getTenancyId(), assignees);
            if (instanceIdList == null || instanceIdList.isEmpty()) {
                return null;
            }
            hisProInstQuery.processInstanceIds(new HashSet<>(instanceIdList));
        }
        // 查询条件：instanceId
        if (StringUtils.isNotBlank(processSearch.getInstanceId())) {
            hisProInstQuery.processInstanceId(processSearch.getInstanceId());
        }
        // 查询条件：processDefinitionKey
        if (StringUtils.isNotBlank(processSearch.getProcessDefinitionKey())) {
            hisProInstQuery.processDefinitionKey(processSearch.getProcessDefinitionKey());
        }
        // 查询条件：processDefinitionId
        if (StringUtils.isNotBlank(processSearch.getProcessDefinitionId())) {
            hisProInstQuery.processDefinitionId(processSearch.getProcessDefinitionId());
        }
        // 查询条件：processDefinitionName
        if (StringUtils.isNotBlank(processSearch.getProcessDefinitionName())) {
            hisProInstQuery.processDefinitionNameLike("%" + StringUtils.trim(processSearch.getProcessDefinitionName()) + "%");
        }
        // 查询条件：businessKey
        if (StringUtils.isNotBlank(processSearch.getBusinessKey())) {
            hisProInstQuery.processInstanceBusinessKeyLike("%" + StringUtils.trim(processSearch.getBusinessKey()) + "%");
        }
        // 查询条件：starterId
        if (StringUtils.isNotBlank(processSearch.getApplyerId())) {
            hisProInstQuery.startedBy(processSearch.getApplyerId());
        }
        // 查询条件：tenantId（支持逗号隔开）
        if (StringUtils.isNotBlank(processSearch.getTenancyId())) {
            String tenantIds = processSearch.getTenancyId();
            tenantIds = CamundaUtil.decode(tenantIds, StandardCharsets.UTF_8.name());
            hisProInstQuery.tenantIdIn(tenantIds.split(","));
        }
        // 查询条件：实例名称
        if (StringUtils.isNotBlank(processSearch.getInstName())) {
            hisProInstQuery.variableValueLike(BpmnVariableConstant.INST_REMARK, "%" + StringUtils.trim(processSearch.getInstName()) + "%");
        }
        // 查询条件：自定义变量【模糊匹配】
        if (StringUtils.isNotBlank(processSearch.getVariablesLike())) {
            Map<String, Object> variablesLikeMap = CamundaUtil.convertJsonStrToMap(processSearch.getVariablesLike());
            variablesLikeMap.forEach((k, v) -> hisProInstQuery.variableValueLike(StringUtils.trim(k), "%" + StringUtils.trim(v.toString()) + "%"));
        }
        // 查询条件：自定义变量【精确】
        if (StringUtils.isNotBlank(processSearch.getVariablesEquals())) {
            Map<String, Object> variablesEqualsMap = CamundaUtil.convertJsonStrToMap(processSearch.getVariablesEquals());
            variablesEqualsMap.forEach((k, v) -> hisProInstQuery.variableValueEquals(StringUtils.trim(k), StringUtils.trim(v.toString())));
        }
        // 查询条件：是否激活/挂起
        if (StringUtils.isNotBlank(processSearch.getActiveStatus())) {
            switch (processSearch.getActiveStatus()) {
                case CommonConstant.INST_ACTIVE:
                    hisProInstQuery.active();
                    break;
                case CommonConstant.INST_SUSPENDED:
                    hisProInstQuery.suspended();
                    break;
                default:
                    break;
            }
        }
        // 查询条件：是否已完成
        if (StringUtils.isNotBlank(processSearch.getFinishedStatus())) {
            switch (processSearch.getFinishedStatus()) {
                case CommonConstant.INST_FINISHED:
                    hisProInstQuery.finished();
                    break;
                case CommonConstant.INST_UNFINISHED:
                    hisProInstQuery.unfinished();
                    break;
                default:
                    break;
            }
        }
        return hisProInstQuery;
    }

    /**
     * 根据接口传递的字段不同排序
     *
     * @param hisProInstQuery 查询实例
     * @param orderString     排序字段
     */
    private void historicProcessInstanceQuerySort(HistoricProcessInstanceQuery hisProInstQuery, String orderString) {
        if (StringUtils.startsWithIgnoreCase(orderString, "processDefinitionId")) {
            hisProInstQuery.orderByProcessDefinitionId();
        } else if (StringUtils.startsWithIgnoreCase(orderString, "processDefinitionKey")) {
            hisProInstQuery.orderByProcessDefinitionKey();
        } else if (StringUtils.startsWithIgnoreCase(orderString, "processDefinitionName")) {
            hisProInstQuery.orderByProcessDefinitionName();
        } else if (StringUtils.startsWithIgnoreCase(orderString, "businessKey")) {
            hisProInstQuery.orderByProcessInstanceBusinessKey();
        } else if (StringUtils.startsWithIgnoreCase(orderString, "startTime")) {
            hisProInstQuery.orderByProcessInstanceStartTime();
        } else if (StringUtils.startsWithIgnoreCase(orderString, "endTime")) {
            hisProInstQuery.orderByProcessInstanceEndTime();
        } else {
            hisProInstQuery.orderByProcessInstanceStartTime().desc();
            return;
        }
        if (StringUtils.endsWithIgnoreCase(orderString, " desc")) {
            hisProInstQuery.desc();
        } else {
            hisProInstQuery.asc();
        }
    }

}
