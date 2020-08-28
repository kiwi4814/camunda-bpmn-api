package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v2/instance/")
@Api(value = "InstanceController", tags = {"「12」 流程修正"})
public class InstanceModificationController {
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ProcessInstanceService processInstanceService;


    @ApiOperation(value = "12.1 流程跳转（建议客户端使用流程图跳转，否则请使用接口1.5）", position = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "运行中的流程实例ID", required = true),
            @ApiImplicitParam(name = "activityId", value = "要跳转的目标ID（节点、网关、线等）", required = true),
            @ApiImplicitParam(name = "variableMap", value = "跳转之前要填充的流程变量，格式为JSON字符串"),
            @ApiImplicitParam(name = "isAfter", value = "是否跳转到目标节点之后(流程从目标位置之后开始继续执行，不包括目标位置）"),
            @ApiImplicitParam(name = "operUser", value = "操作人"),
            @ApiImplicitParam(name = "operType", value = "操作类型，不传的话默认为4（转向）"),
            @ApiImplicitParam(name = "reason", value = "操作说明")
    })
    @GetMapping(value = "/executeModification.json")
    public Result executeModification(@RequestParam("instanceId") String instanceId,
                                      @RequestParam("activityId") String activityId,
                                      String variableMap, String isAfter,
                                      String operUser, String operType, String reason) {
        Result result = new Result();
        try {
            processInstanceService.executeProcessModification(instanceId, activityId, variableMap, isAfter, operUser, operType, reason);
            // 返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
            Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(instanceId, null);
            result.setData(resultMap);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "12.2 被打回的流程恢复到开始节点", position = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "运行中的流程实例ID", required = true),
            @ApiImplicitParam(name = "variableMap", value = "跳转之前要填充的流程变量，格式为JSON字符串"),
            @ApiImplicitParam(name = "operUser", value = "操作人"),
            @ApiImplicitParam(name = "operType", value = "操作类型，不传的话默认为3（恢复）"),
            @ApiImplicitParam(name = "reason", value = "操作说明")
    })
    @GetMapping(value = "/restoreProcessToStart.json")
    public Result restoreProcessToStart(@RequestParam("instanceId") String instanceId, String variableMap,
                                        String operUser, String operType, String reason) {
        Result result = new Result();
        try {
            processInstanceService.activeAndModifyToStart(instanceId, variableMap, operUser, operType, reason);
            // 返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
            Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(instanceId, null);
            result.setData(resultMap);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "12.3 将流程实例从一个流程定义迁移到另外一个流程定义", position = 3)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sourceDefinition", value = "源流程定义ID", required = true),
            @ApiImplicitParam(name = "targetDefinition", value = "目标流程定义ID", required = true),
            @ApiImplicitParam(name = "instanceId", value = "要迁移的流程实例", required = true)
    })
    @GetMapping(value = "/executeMigration.json")
    public Result executeMigration(@RequestParam("sourceDefinition") String sourceDefinition,
                                   @RequestParam("targetDefinition") String targetDefinition,
                                   @RequestParam("instanceId") String instanceId) {
        Result result = new Result();
        try {
            MigrationPlan migrationPlan = runtimeService
                    .createMigrationPlan(sourceDefinition, targetDefinition)
                    .mapEqualActivities()
                    .updateEventTriggers()
                    .build();
            runtimeService.newMigration(migrationPlan)
                    .processInstanceIds(instanceId)
                    .execute();
        } catch (ProcessEngineException e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            result.setMessage(CamundaUtil.getRealMessage(e));
        }
        return result;
    }
}
