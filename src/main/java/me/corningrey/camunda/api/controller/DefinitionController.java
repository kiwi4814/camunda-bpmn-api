package me.corningrey.camunda.api.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessDefinitionService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/definition/")
@Api(value = "DefinitionController", tags = {"「01」 流程定义相关接口"})
public class DefinitionController {
    @Resource
    private RepositoryService repositoryService;
    /*@Resource
    private TenantProcessService tenantProcessService;*/
    @Resource
    private ProcessDefinitionService processDefinitionService;

    @ApiOperation(value = "1.7 部署一个流程定义", position = 7)
    // @ApiOperationSupport(order = 7)
    @PostMapping(value = "/deployProcessDefinition.json")
    public Result deployProcessDefinition(@ModelAttribute("processDoploy") ProcessDoploy processDoploy) {
        Result result = new Result();
        DeploymentWithDefinitions deployment = null;
        InputStream byteArrayInputStream = null;
        try {
            if (CamundaUtil.checkResourceSuffix(processDoploy.getFilePath())) {
                String resourceName = CamundaUtil.operResourceName(processDoploy.getResourceName(), (processDoploy.getFilePath()));
                byteArrayInputStream = new FileInputStream(processDoploy.getFilePath());
                deployment = repositoryService.createDeployment().addInputStream(resourceName, byteArrayInputStream)
                        .name(processDoploy.getDeployName()).tenantId(processDoploy.getTenant()).deployWithResult();
            }
            if (CamundaUtil.checkResourceSuffix(processDoploy.getResourcePath())) {
                deployment = repositoryService.createDeployment().addClasspathResource(processDoploy.getResourcePath())
                        .name(processDoploy.getDeployName()).tenantId(processDoploy.getTenant()).deployWithResult();
            }
            if (deployment != null) {
                // 目前仅支持一次部署一个流程定义
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
                if (pd != null) {
                    //tenantProcessService.insert(TenantProcess.builder().tenantId(processDoploy.getTenant()).processDefinitionId(pd.getId()).processName(pd.getName()).filePath(processDoploy.getFilePath()).createUser(processDoploy.getCurrentUser()).build());
                    MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
                    ProcessDefinitionExt processDefinitionExt = mapperFactory.getMapperFacade().map(pd, ProcessDefinitionExt.class);
                    result.setData(processDefinitionExt);
                }
                result.setResultCode(ResultEnum.SUCCESS.getValue());

            } else {
                result.setResultCode(ResultEnum.FAIL.getValue());
                result.setMessage("部署失败，请检查路径下文件是否存在或符合规范！");
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            result.setMessage("部署失败，请检查路径下文件是否存在或符合规范！");
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
        }
        return result;
    }

    @ApiOperation(value = "1.8 下载流程定义文件", position = 8)
    // @ApiOperationSupport(order = 8)
    @RequestMapping(value = "/downloadProcessDefinitionFile.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID", required = true, dataType = "String")
    public ResponseEntity<byte[]> downloadProcessDefinitionFile(String processDefinitionId) {

        InputStream is = null;
        byte[] body = null;
        HttpHeaders headers = new HttpHeaders();
        try {
            // 将inputStream转换为byte[]
            is = repositoryService.getProcessModel(processDefinitionId);
            body = IOUtils.toByteArray(is);
            // 查询流程定义名称，用作文件名
            ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
            pdq.processDefinitionId(processDefinitionId);
            ProcessDefinition pd = pdq.singleResult();
            String fileName = pd.getResourceName();
            headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            UnitedLogger.error(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return new ResponseEntity<>(body, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "1.2 查询流程定义（不推荐使用）", position = 2)
    // @ApiOperationSupport(order = 2)
    @GetMapping(value = "/findProcessDefinitionList.json")
    public Result findProcessDefinitionList(@ModelAttribute("processSearch") ProcessSearch processSearch) {
        Result result = new Result();
        try {
            ProcessDefinitionQuery pd = repositoryService.createProcessDefinitionQuery();
            // 查询条件：processDefinitionId
            if (StringUtils.isNotBlank(processSearch.getProcessDefinitionId())) {
                pd.processDefinitionId(processSearch.getProcessDefinitionId());
            }
            // 查询条件：processDefinitionKey
            if (StringUtils.isNotBlank(processSearch.getProcessDefinitionKey())) {
                pd.processDefinitionKey(processSearch.getProcessDefinitionKey());
            }
            // 查询条件：processDefinitionName
            if (StringUtils.isNotBlank(processSearch.getProcessDefinitionName())) {
                pd.processDefinitionNameLike("%" + StringUtils.trim(processSearch.getProcessDefinitionName()) + "%");
            }
            // 查询条件：tenantId
            if (StringUtils.isNotBlank(processSearch.getTenancyId())) {
                pd.tenantIdIn(processSearch.getTenancyId().split(","));
            }
            // 除非isLastVersion为false，否则默认只查询最新版本的流程定义
            if (processSearch.getIsLastVersion() == null || processSearch.getIsLastVersion()) {
                pd.latestVersion();
            }
            // 默认排序规则
            pd.orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc();

            // 开始分页
            Map<String, Object> resultMap = CamundaUtil.getCamundaParamer(processSearch.getPageNum(), processSearch.getPageLimit(), pd);

            // 重新封装
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            List<ProcessDefinitionExt> resultList = mapperFactory.getMapperFacade().mapAsList((Iterable) resultMap.get("list"), ProcessDefinitionExt.class);
            resultMap.put("list", resultList);

            result.setData(resultMap);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "1.1 根据流程定义类型查询流程定义列表", position = 1)
    // @ApiOperationSupport(order = 1)
    @GetMapping(value = "/findProcessDefinitionWithType.json")
    public Result findProcessDefinitionWithType(@ModelAttribute("processSearch") ProcessSearch processSearch) {
        Result result = new Result();
        try {
            // 对支持逗号拼接的参数进行解码
            String processType = processSearch.getProcessType();
            if (StringUtils.isNotBlank(processType)) {
                processSearch.setProcessType(CamundaUtil.decode(processType, StandardCharsets.UTF_8.name()));
            }
            List<ProcessDefinitionExt> resultList = processDefinitionService.findProcessDefinitionWithType(processSearch);
            result.setData(resultList);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
        }

        return result;
    }

    @ApiOperation(value = "1.3 根据流程定义加载xml文件", position = 3)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID"),
            @ApiImplicitParam(name = "processDefinitionKey", value = "流程定义key")
    })
    // @ApiOperationSupport(order = 3)
    @GetMapping(value = "/loadXmlByProcessDefinition.json")
    public Result loadXmlByProcessDefinition(@RequestParam(value = "processDefinitionId", defaultValue = "") String processDefinitionId,
                                             @RequestParam(value = "processDefinitionKey", defaultValue = "") String processDefinitionKey) {
        Result result = new Result();
        String processXml = "";
        byte[] processBytes = null;
        InputStream resourceAsStream = null;
        try {
            ProcessDefinitionQuery pd = repositoryService.createProcessDefinitionQuery();
            ProcessDefinition processDefinition = null;
            // 优先判断processDefinitionId
            if (StringUtils.isNotBlank(processDefinitionId)) {
                processDefinition = pd.processDefinitionId(processDefinitionId).singleResult();
            } else if (StringUtils.isNotBlank(processDefinitionKey)) {
                List<ProcessDefinition> processDefinitionList = pd.processDefinitionKey(processDefinitionKey).orderByProcessDefinitionVersion().desc().list();
                processDefinition = processDefinitionList.get(0);
            }
            if (processDefinition != null) {
                resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
                StringWriter writer = new StringWriter();
                IOUtils.copy(resourceAsStream, writer, StandardCharsets.UTF_8.name());
                processXml = writer.toString();
                processBytes = IOUtils.toByteArray(resourceAsStream);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("processXml", processXml);
            map.put("processBytes", processBytes);
            result.setData(map);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return result;
    }

    @ApiOperation(value = "1.4 查询流程定义的用户节点列表（用于V1跳转）", position = 4)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID"),
            @ApiImplicitParam(name = "activityId", value = "活动ID，表示查询从此节点之后开始递归【适用于跳向未执行节点的情况】")
    })
    // @ApiOperationSupport(order = 4)
    @GetMapping(value = "/selectUserTaskAfter.json")
    public Result selectUserTaskAfter(@RequestParam("processDefinitionId") String processDefinitionId, @RequestParam(name = "activityId", required = false) String activityId) {
        Result result = new Result();
        try {
            List<TaskNode> taskNodes = new ArrayList<>();
            BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
            // 没传activityId的情况下，将返回流程定义中所有的用户任务节点信息
            if (StringUtils.isBlank(activityId)) {
                taskNodes = new ArrayList<>();
                Collection<ModelElementInstance> userTasks = modelInstance.getModelElementsByType(modelInstance.getModel().getType(UserTask.class));
                List<TaskNode> finalTaskNodes = taskNodes;
                userTasks.forEach(t -> {
                    FlowNode flowNode = (FlowNode) t;
                    finalTaskNodes.add(TaskNode.builder().name(flowNode.getName()).id(flowNode.getId())
                            .incoming(Optional.ofNullable(flowNode.getIncoming()).map(k -> k.iterator().next().getId()).orElse(""))
                            .build());
                });
            } else {
                ModelElementInstance elementInstance = modelInstance.getModelElementById(activityId);
                List<String> sequenceFlows = new ArrayList<>();
                if (elementInstance instanceof SequenceFlow) {
                    CamundaUtil.getUserTaskAfter((SequenceFlow) elementInstance, taskNodes, sequenceFlows);
                } else {
                    FlowNode flowNode = (FlowNode) elementInstance;
                    List<TaskNode> finalTaskNodes = taskNodes;
                    flowNode.getOutgoing().forEach(t -> CamundaUtil.getUserTaskAfter(t, finalTaskNodes, sequenceFlows));
                }
            }
            taskNodes = taskNodes.stream().distinct().collect(Collectors.toList());
            result.setData(taskNodes);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
        }

        return result;
    }

    @ApiOperation(value = "1.6 查询流程定义中单个节点的信息", position = 6)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID"),
            @ApiImplicitParam(name = "activityId", value = "活动ID（网关、线、节点）")
    })
    // @ApiOperationSupport(order = 6)
    @GetMapping(value = "/selectActivityInfo.json")
    public Result selectActivityInfo(@RequestParam("processDefinitionId") String processDefinitionId, @RequestParam(name = "activityId") String activityId) {
        Result result = new Result();
        try {
            BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
            FlowElement flowElement = modelInstance.getModelElementById(activityId);
            BpmnElement bpmnElement = new BpmnElement();
            bpmnElement.setActivityId(flowElement.getId());
            bpmnElement.setActivityName(flowElement.getName());
            bpmnElement.setActivityType(flowElement.getElementType().getTypeName());
            if (flowElement instanceof SequenceFlow) {
                SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                bpmnElement.setCompletionExp(sequenceFlow.getConditionExpression().getTextContent());
            } else if (flowElement instanceof ExclusiveGateway) {
                ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
                Collection<SequenceFlow> list = exclusiveGateway.getOutgoing();
                List<BpmnElement> sequenceList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : list) {
                    ConditionExpression text = sequenceFlow.getConditionExpression();
                    sequenceList.add(BpmnElement.builder().activityName(sequenceFlow.getName()).activityId(sequenceFlow.getId()).activityType(sequenceFlow.getElementType().getTypeName())
                            .completionExp(text.getTextContent()).build());
                }
                bpmnElement.setSequenceList(sequenceList);
            }
            result.setData(bpmnElement);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
        }

        return result;
    }

    @ApiOperation(value = "1.5 查询流程定义中所有节点信息（用于V2跳转）", position = 5)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID")
    })
    // @ApiOperationSupport(order = 5)
    @GetMapping(value = "/selectProcessNodeList.json")
    public Result selectProcessNodeList(@RequestParam("processDefinitionId") String processDefinitionId) {
        Result result = new Result();
        InputStream resourceAsStream = null;
        try {
            BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
            String xml = Bpmn.convertToString(bpmnModelInstance);
            Document document = DocumentHelper.parseText(xml);
            List<JSONObject> nodeList = new ArrayList<>();
            List<Node> taskList = document.selectNodes("/bpmn:definitions/bpmn:process/*");
            taskList.forEach(t -> {
                JSONObject jsonObject = new JSONObject();
                Element e = (Element) t;
                jsonObject.put("id", e.attributeValue("id"));
                jsonObject.put("name", e.attributeValue("name"));
                jsonObject.put("type", e.getName());
                nodeList.add(jsonObject);
            });
            result.setData(nodeList);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return result;
    }

    @ApiOperation(value = "1.3 根据流程定义查询XML", position = 9)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID")
    })
    // @ApiOperationSupport(order = 9)
    @GetMapping(value = "/parseBpmnXmlByDefinition.json")
    public Result parseBpmnXmlByDefinition(@RequestParam("processDefinitionId") String processDefinitionId) {
        Result result = new Result();
        try {
            BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
            Bpmn.validateModel(bpmnModelInstance);
            result.setData(Bpmn.convertToString(bpmnModelInstance));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (ProcessEngineException e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }
        return result;
    }
}
