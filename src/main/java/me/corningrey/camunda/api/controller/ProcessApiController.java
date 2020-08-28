package me.corningrey.camunda.api.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.service.ProcessSettingApiService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/process/")
@Api(value = "GCProcessApiController", tags = {"「11」 设置查询"}, hidden = true)
public class ProcessApiController {
    @Resource
    private ProcessSettingApiService processSettingApiService;

    @ApiOperation(value = "11.1 根据流程定义和节点定义查询用户列表", position = 1)
    // @ApiOperationSupport(order = 1)
    @GetMapping(value = "/findUserListByDefinition.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID"),
            @ApiImplicitParam(name = "taskDefinitionKey", value = "节点定义Key")
    })
    public Result findUserListByTaskId(@RequestParam("processDefinitionId") String processDefinitionId, @RequestParam("taskDefinitionKey") String taskDefinitionKey) {
        Result result = new Result();
        try {
            List<String> userList = processSettingApiService.getUserListByTaskSetting(processDefinitionId, taskDefinitionKey, new HashMap<>());
            result.setData(userList);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "", hidden = true)
    @GetMapping(value = "/test.json")
    public Result test(@RequestParam Map<String, String> map) {
        Result result = new Result();
        try {
            System.out.println(map);
            /*if (1 == 1) {
                throw new UnitedException("测试异常");
            }*/
            result.setData(map);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.ERROR.getValue());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "", hidden = true)
    @RequestMapping(value = "/selectApplicant.json", method = RequestMethod.GET)
    public Result selectApplicant(HttpServletRequest request, String appSecret, @RequestParam(value = "applicant", defaultValue = "") String applicant) {
        Result result = new Result();
        try {
            /*if (1 == 1) {
                throw new UnitedException("测试异常");
            }*/
            String jsonArrayStr = "{\"list\":[{\"userId\":\"" + applicant + "\",\"userName\":\"applicant\"}]}";
            result.setData(JSONObject.parse(jsonArrayStr));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setMessage(e.getMessage());
            result.setMessageCode(e.getMessage());
            result.setResultCode(ResultEnum.FAIL.getValue());
        }
        return result;
    }


}
