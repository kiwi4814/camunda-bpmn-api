
package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.service.StatisticsService;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/statistics/")
@Api(value = "StatisticsController", tags = {"「09」 流程数据统计相关接口"})
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 根据租户id和用户id查询一个系统的流程统计数据
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    @ApiOperation(value = "9.1 根据租户id和用户id查询一个系统的流程统计数据", position = 1)
    // @ApiOperationSupport(order = 1)
    @GetMapping(value = "/findCommonData.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户id"),
            @ApiImplicitParam(name = "userId", value = "用户id")
    })
    public Result findCommonData(String tenantId, String userId) {
        Result result = new Result();
        try {
            result.setData(statisticsService.findCommonDataFromDatabase(tenantId, userId));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
            return result;
        }
        return result;
    }
}
