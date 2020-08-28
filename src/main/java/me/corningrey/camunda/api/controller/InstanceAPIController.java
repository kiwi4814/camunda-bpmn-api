package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.service.InstanceAPIService;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.ProcessInstanceExt;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import me.corningrey.camunda.api.model.ProcessSearch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v2/instance/")
@Api(value = "InstanceController", tags = {"「02」 流程实例相关接口v2"})
public class InstanceAPIController {
    @Resource
    private InstanceAPIService instanceAPIService;

    @ApiOperation(value = "2.2 查询流程实例操作日志", position = 2)
    // @ApiOperationSupport(order = 2)
    @GetMapping(value = "/findProcessOperHistoryList.json")
    public Result<PageResult<ProcessOperHistory>> findProcessOperHistoryList(@ModelAttribute("operHistorySearch") OperHistorySearch operHistorySearch) {
        Result<PageResult<ProcessOperHistory>> result = new Result<>();

        try {
            result.setData(instanceAPIService.findProcessOperHistoryList(operHistorySearch));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "2.1 查询流程实例列表", position = 1)
    // @ApiOperationSupport(order = 1)
    @GetMapping(value = "/findHisInstanceList.json")
    public Result<PageResult<ProcessInstanceExt>> findHisInstanceList(@ModelAttribute("processSearch") ProcessSearch processSearch) {
        Result<PageResult<ProcessInstanceExt>> result = new Result<>();
        try {
            result.setData(instanceAPIService.findHisInstanceList(processSearch));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }
        return result;
    }
}
