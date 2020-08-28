package me.corningrey.camunda.api.controller;

import cn.hutool.core.util.StrUtil;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v2/variable/")
@Api(value = "VariableController", tags = {"「07」 流程变量相关接口v2"})
public class VariableController {

    @Resource
    private RuntimeService runtimeService;

    @ApiOperation(value = "7.1 根据流程实例ID修改单个流程变量", position = 1)
    // @ApiOperationSupport(order = 1)
    @PostMapping(value = "/setVariable.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "流程实例ID", required = true),
            @ApiImplicitParam(name = "key", value = "流程变量Key", required = true),
            @ApiImplicitParam(name = "type", value = "流程变量类型（数字、文本、布尔、日期、列表）",
                    allowEmptyValue = true, allowableValues = "text,number,boolean,date,list"),
            @ApiImplicitParam(name = "value", value = "流程变量Value")
    })
    public Result setVariable(String instanceId, String key, String type, String value) {
        Result result = new Result();
        try {
            if (StrUtil.isNotBlank(key)) {
                Object realV = CamundaUtil.getRealTypeValue(type, value);
                runtimeService.setVariable(instanceId, key, realV);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            }
        } catch (ProcessEngineException e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getLocalizedMessage());
        }
        return result;
    }

    @ApiOperation(value = "7.2 根据流程实例ID批量修改流程变量（自动判断类型并转换）", position = 2)
    // @ApiOperationSupport(order = 2)
    @PostMapping(value = "/setVariables.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "流程实例ID", required = true),
            @ApiImplicitParam(name = "variables", value = "流程变量JSON字符串")
    })
    public Result setVariables(String instanceId, String variables) {
        Result result = new Result();
        try {
            Map<String, Object> variableMap = CamundaUtil.convertJsonStrToMap(variables);
            if (!variableMap.isEmpty()) {
                CamundaUtil.resetVarTypeAuto(variableMap);
                runtimeService.setVariables(instanceId, variableMap);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            }
        } catch (ProcessEngineException e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getLocalizedMessage());
        }
        return result;
    }

    @ApiOperation(value = "7.3 根据流程实例ID删除流程变量", position = 3)
    // @ApiOperationSupport(order = 3)
    @PostMapping(value = "/removeVariables.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "instanceId", value = "流程实例ID", required = true),
            @ApiImplicitParam(name = "variables", value = "流程变量逗号拼接字符串")
    })
    public Result removeVariables(String instanceId, String variables) {
        Result result = new Result();
        try {
            variables = CamundaUtil.decode(variables, StandardCharsets.UTF_8.name());
            if (StringUtils.isNotBlank(variables)) {
                List<String> removeVariables = Arrays.asList(variables.split(","));
                runtimeService.removeVariables(instanceId, removeVariables);
                result.setResultCode(ResultEnum.SUCCESS.getValue());
            }
        } catch (ProcessEngineException e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getLocalizedMessage());
        }
        return result;
    }


}
