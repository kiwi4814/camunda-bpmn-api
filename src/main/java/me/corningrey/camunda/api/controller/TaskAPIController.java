
package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.TaskAPISearch;
import me.corningrey.camunda.api.service.TaskAPIService;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.TaskExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v2/task")
@Api(value = "TaskAPIController", tags = {"「04」 待办相关接口v2"})
public class TaskAPIController {
    @Resource
    private TaskAPIService taskAPIService;
    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;


    @ApiOperation(value = "4.1 查询未完成待办列表", position = 1)
    // @ApiOperationSupport(order = 1)
    @GetMapping(value = "/todoTasks.json")
    public Result<PageResult<TaskExt>> todoTasks(@ModelAttribute("taskSearch") TaskAPISearch taskSearch) {
        Result<PageResult<TaskExt>> res = new Result<>();
        try {
            res.setData(taskAPIService.findTodoTasks(taskSearch));
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            UnitedLogger.error("查询失败！", e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "4.2 查询所有待办列表", position = 2)
    // @ApiOperationSupport(order = 2)
    @GetMapping(value = "/findTasks.json")
    public Result<PageResult<TaskExt>> findTasks(@ModelAttribute("taskSearch") TaskAPISearch taskSearch) {
        Result<PageResult<TaskExt>> res = new Result<>();
        try {
            res.setData(taskAPIService.findAllTasks(taskSearch));
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            UnitedLogger.error("查询失败！", e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "4.3 查询单个待办详情", hidden = true)
    // @ApiOperationSupport(order = 3)
    @GetMapping(value = "/findSingleTask.json")
    public Result<TaskExt> findSingleTask(@RequestParam("taskId") String taskId) {
        Result<TaskExt> res = new Result<>();
        try {
            TaskExt taskExt = taskAPIService.findSingleTask(taskId);
            String taskdefinitionKey = taskExt.getTaskDefinitionKey();
            /*historyService.createHistoricVariableInstanceQuery().processInstanceId(taskExt.getInstanceId())
                    .variableName()
            res.setData();*/
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            UnitedLogger.error("查询失败！", e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }
}
