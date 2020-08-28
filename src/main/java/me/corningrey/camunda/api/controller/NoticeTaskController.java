package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.service.NoticeTaskService;
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

@RestController
@RequestMapping(value = "/api/sign/")
@Api(value = "NoticeTaskController", tags = {"「10」 传阅操作"}, hidden = true)
public class NoticeTaskController {
    @Resource
    private NoticeTaskService noticeTaskService;

    @ApiOperation(value = "10.1 新增传阅", position = 1)
    // @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID", required = true),
            @ApiImplicitParam(name = "assignees", value = "传阅审批人，多个用户逗号拼接", required = true),
            @ApiImplicitParam(name = "description", value = "针对传阅的描述（非必填）"),
            @ApiImplicitParam(name = "operUser", value = "操作人，用于存储日志（默认为当前待办的所有者，不包括代理人）")
    })
    @PostMapping(value = "/addNoticeTask.json")
    public Result addNoticeTask(@RequestParam("taskId") String taskId,
                                @RequestParam("assignees") String assignees,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "operUser", required = false) String operUser
    ) {
        Result res = new Result();
        try {
            noticeTaskService.insertNoticeTask(taskId, description, assignees, operUser);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }

    @ApiOperation(value = "10.2 审批传阅", position = 2)
    // @ApiOperationSupport(order = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "传阅的ID", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作人"),
            @ApiImplicitParam(name = "reason", value = "传阅人审批时候的意见（非必填）"),
    })
    @PostMapping(value = "/approvalNoticeTask.json")
    public Result approvalNoticeTask(@RequestParam("id") String id,
                                     @RequestParam(value = "operUser", required = false) String operUser,
                                     @RequestParam(value = "reason", required = false) String reason
    ) {
        Result res = new Result();
        try {
            noticeTaskService.approvalNoticeTask(id, operUser, reason);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }


    @ApiOperation(value = "10.3 传阅修改审批人", position = 3)
    // @ApiOperationSupport(order = 3)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "传阅的ID", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作人", required = true),
            @ApiImplicitParam(name = "assignee", value = "审批人人"),
    })
    @PostMapping(value = "/updateAssigneeNoticeTask.json")
    public Result updateAssigneeNoticeTask(@RequestParam("id") String id,
                                           @RequestParam(value = "assignee") String assignee,
                                           @RequestParam(value = "operUser", required = false) String operUser

    ) {
        Result res = new Result();
        try {
            noticeTaskService.updateAssigneeNoticeTask(id, operUser, assignee);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }

    @ApiOperation(value = "10.4 删除传阅接口", position = 4)
    // @ApiOperationSupport(order = 4)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "传阅的ID", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作人", required = true),
    })
    @PostMapping(value = "/deletAssigneeNoticeTask.json")
    public Result updateAssigneeNoticeTask(@RequestParam("id") String id,
                                           @RequestParam(value = "operUser", required = false) String operUser

    ) {
        Result res = new Result();
        try {
            noticeTaskService.deletAssigneeNoticeTask(id, operUser);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }
}
