
package me.corningrey.camunda.api.controller;

import com.alibaba.fastjson.JSON;
import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.service.TaskSignService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/sign/")
@Api(value = "TaskSignController", tags = {"「08」 加签相关接口"})
public class TaskSignController {
    @Resource
    private TaskSignService taskSignService;
    @Resource
    private ProcessInstanceService processInstanceService;


    @ApiOperation(value = "8.1 为指定节点增加审批人【推荐使用8.2】", position = 1)
    // @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID"),
            @ApiImplicitParam(name = "signUsers", value = "加签用户，多个用户逗号拼接")
    })
    @PostMapping(value = "/runningPlusSign.json")
    public Result multiSign(@RequestParam("taskId") String taskId, @RequestParam("signUsers") String signUsers) {
        Result res = new Result();
        res.setData(taskSignService.runningPlusSign(taskId, signUsers));
        res.setResultCode(ResultEnum.SUCCESS.getValue());
        return res;
    }

    @ApiOperation(value = "8.2 为指定节点增加审批人并返回最新流程信息", position = 2)
    // @ApiOperationSupport(order = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID", required = true),
            @ApiImplicitParam(name = "signUsers", value = "加签用户，多个用户逗号拼接", required = true),
            @ApiImplicitParam(name = "reason", value = "加签原因（非必填）"),
            @ApiImplicitParam(name = "operUser", value = "操作人，用于存储日志（默认为当前待办的所有者，不包括代理人）")
    })
    @PostMapping(value = "/v2/runningPlusSign.json")
    public Result manualSignTasks(@RequestParam("taskId") String taskId,
                                  @RequestParam("signUsers") String signUsers,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  @RequestParam(value = "operUser", required = false) String operUser
    ) {
        Result res = new Result();
        try {
            List<String> taskIds = taskSignService.manualSignTasks(taskId, signUsers, reason, operUser);
            // 待办审批完成之后，返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
            Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(null, taskId);
            resultMap.put("taskIds", JSON.toJSONString(taskIds));
            res.setData(resultMap);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }


/*    @ApiOperation(value = "保存加签节点", notes = " ")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID"),
            @ApiImplicitParam(name = "assignees", value = "加签用户，多个用户逗号拼接")
    })
    @PostMapping(value = "/savePlusSignNode.json")
    public Result savePlusSignNode(@RequestParam("taskId") String taskId, @RequestParam("assignees") String assignees) {
        Result res = new Result();
        taskSignService.runningPlusSign(taskId, assignees);
        res.setResultCode(ResultEnum.SUCCESS.getValue());
        return res;
    }*/

}
