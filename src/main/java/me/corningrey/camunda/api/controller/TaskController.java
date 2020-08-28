
package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.service.TaskAgentService;
import me.corningrey.camunda.api.service.TaskOperService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import me.corningrey.camunda.api.model.TaskExt;
import me.corningrey.camunda.api.model.TaskSearch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/task")
@Api(value = "TaskController", tags = {"「05」 待办处理相关接口"})
public class TaskController {
    @Resource
    private TaskService taskService;
    @Resource
    TaskOperService taskOperService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskAgentService taskAgentService;
    @Resource
    private ProcessInstanceService processInstanceService;

    @ApiOperation(value = "5.6 设置代理人", position = 6)
    // @ApiOperationSupport(order = 6)
    @PostMapping(value = "/setAgent.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作用户", required = true),
            @ApiImplicitParam(name = "principal", value = "被代理人", required = true),
            @ApiImplicitParam(name = "agent", value = "代理人", required = true),
            @ApiImplicitParam(name = "reason", value = "代理原因")
    })
    public Result setAgent(String taskId, String operUser, String principal, String agent, String reason) {
        Result res = new Result();
        try {
            taskAgentService.setAgent(taskId, operUser, principal, agent, reason);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage(e.getMessage());
            return res;
        }
        return res;
    }

    @ApiOperation(value = "5.10 根据executionId修改流程变量值", position = 10)
    // @ApiOperationSupport(order = 10)
    @PostMapping(value = "/setVariablesByExecutionId.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "executionId", value = "执行ID", required = true),
            @ApiImplicitParam(name = "key", value = "变量Key", required = true),
            @ApiImplicitParam(name = "value", value = "变量值", required = true)
    })
    public Result setVariablesByExecutionId(String executionId, String key, String value) {
        Result res = new Result();
        if (StringUtils.isBlank(key)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("key not found！");
            return res;
        }
        if (StringUtils.isBlank(executionId)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("executionId not found！");
            return res;
        }
        //变量设置
        value = CamundaUtil.decode(value, StandardCharsets.UTF_8.name());
        runtimeService.setVariable(executionId, key, value);
        res.setResultCode(ResultEnum.SUCCESS.getValue());
        return res;
    }

    @ApiOperation(value = "5.9 根据taskId修改流程变量值", position = 9)
    // @ApiOperationSupport(order = 9)
    @PostMapping(value = "/setVariablesByTaskId.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办任务ID", required = true),
            @ApiImplicitParam(name = "key", value = "变量Key", required = true),
            @ApiImplicitParam(name = "value", value = "变量值", required = true)
    })
    public Result setVariablesByTaskId(String taskId, String key, String value) {
        Result res = new Result();
        if (StringUtils.isBlank(key)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("key not found！");
            return res;
        }
        if (StringUtils.isBlank(taskId)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("taskId not found！");
            return res;
        }

        try {
            //变量设置
            value = CamundaUtil.decode(value, StandardCharsets.UTF_8.name());
            taskService.setVariable(taskId, key, value);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "5.8 根据taskId获取流程变量值", position = 8)
    // @ApiOperationSupport(order = 8)
    @GetMapping(value = "/getVariablesByTaskId.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办任务ID", required = true),
            @ApiImplicitParam(name = "variables", value = "变量值", required = true)
    })
    public Result getVariablesByTaskId(String taskId, String variables) {
        Result res = new Result();

        if (StringUtils.isBlank(taskId)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("taskId not found！");
            return res;
        }
        if (StringUtils.isBlank(variables)) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage("variables not found！");
            return res;
        }

        try {
            variables = CamundaUtil.decode(variables, StandardCharsets.UTF_8.name());
            List<String> variableList = Arrays.asList(variables.split(","));
            Map<String, Object> map = taskService.getVariables(taskId, variableList);

            res.setResultCode(ResultEnum.SUCCESS.getValue());
            res.setData(map);
        } catch (Exception e) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "5.5 执行待办任务转办操作", position = 5)
    // @ApiOperationSupport(order = 5)
    @PostMapping(value = "/setAssignee.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办ID", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作用户", required = true),
            @ApiImplicitParam(name = "inheritor", value = "转办人", required = true),
            @ApiImplicitParam(name = "reason", value = "转办原因")
    })
    public Result setAssignee(String taskId, String operUser, String inheritor, String reason) {
        Result res = new Result();
        try {
            taskAgentService.setAssignee(taskId, operUser, inheritor, reason);
            // 待办审批完成之后，返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
            Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(null, taskId);
            res.setData(resultMap);
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            res.setResultCode(ResultEnum.FAIL.getValue());
            res.setMessage(e.getMessage());
            return res;
        }
        return res;
    }


    @ApiOperation(value = "5.1 待办审批", position = 1)
    // @ApiOperationSupport(order = 1)
    @PostMapping(value = "/audit.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办任务ID", required = true),
            @ApiImplicitParam(name = "assignee", value = "审批人"),
            @ApiImplicitParam(name = "action", value = "审批结果"),
            @ApiImplicitParam(name = "comment", value = "审批意见"),
            @ApiImplicitParam(name = "optionalUsers", value = "可选用户"),
            @ApiImplicitParam(name = "variables", value = "变量")
    })
    public Result audit(String taskId, String assignee, String action, String comment,
                        Integer completionLevel, String optionalUsers, String variables) {
        //instance不为空，说明流程发起成功
        Result res = new Result();
        try {
            if (StringUtils.isNotBlank(taskId)) {
                taskOperService.audit(taskId, assignee, action, comment,
                        completionLevel, optionalUsers, CamundaUtil.convertJsonStrToMap(variables));
                // 待办审批完成之后，返回流程实例ID（processInstanceId）、流程最新状态（instanceState）、当前正在审批的待办TaskDefinitionKey(currentTaskDefKey)
                Map<String, String> resultMap = processInstanceService.findProcessInstanceStatus(null, taskId);
                res.setData(resultMap);
                res.setResultCode(ResultEnum.SUCCESS.getValue());
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(CamundaUtil.getRealMessage(e));
        }
        return res;
    }


    @ApiOperation(value = "5.3 查询历史待办列表（默认只查询已完成待办,推荐使用4.2）", position = 3)
    // @ApiOperationSupport(order = 3)
    @GetMapping(value = "/findTasks.json")
    public Result findTasks(@ModelAttribute("taskSearch") TaskSearch taskSearch) {
        Result res = new Result();
        try {
            res.setData(taskOperService.findTasks(taskSearch));
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            UnitedLogger.error("查询失败！", e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "5.2 查询未完成待办列表（推荐使用4.1）", position = 2)
    // @ApiOperationSupport(order = 2)
    @GetMapping(value = "/todoTasks.json")
    public Result todoTasks(@ModelAttribute("taskSearch") TaskSearch taskSearch) {
        Result res = new Result();
        List<TaskExt> taskList;
        try {
            taskList = taskOperService.findToDoTasks(taskSearch);
            res.setData(CamundaUtil.getSimpleParamer(taskList, taskSearch.getPageNum(), taskSearch.getPageLimit()));
            res.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (UnitedException e) {
            UnitedLogger.error("查询失败！", e);
            res.setResultCode(ResultEnum.ERROR.getValue());
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @ApiOperation(value = "5.4 查询单个待办详情(当前任务/历史任务)", position = 4)
    // @ApiOperationSupport(order = 4)
    @GetMapping(value = "/selectTaskInfoById.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办任务ID", required = true),
            @ApiImplicitParam(name = "variableKeys", value = "变量名称")
    })
    public Result selectTaskInfoById(@RequestParam(name = "taskId") String taskId, @RequestParam(name = "variableKeys", required = false) String variableKeys) {
        Result result = new Result();
        try {
            // 参数验证
            if (StringUtils.isBlank(taskId)) {
                result.setResultCode(ResultEnum.FAIL.getValue());
                result.setMessage("taskId is null！");
            } else {
                TaskExt taskExt = null;
                //查询当前进行中的任务
                Task t = taskService.createTaskQuery().taskId(taskId).singleResult();
                if (t != null) {
                    //重新封装实体
                    taskExt = taskOperService.getTaskExtByBpmnTask(t.getProcessDefinitionId(), t.getProcessInstanceId(), taskId, t.getTaskDefinitionKey(), variableKeys);
                    // 查询当前待办是否为审批节点的最后一个待办
                    Object isFinalApprover = taskService.getVariable(taskId, taskExt.getTaskDefinitionKey().concat(BpmnVariableConstant.SUFFIX_IS_FINAL_APPROVER));
                    taskExt.setIsFinalApprover(Optional.ofNullable(isFinalApprover).map(Object::toString).orElse(""));
                } else {
                    //查询历史任务
                    HistoricTaskInstance hisTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
                    //重新封装实体
                    if (hisTask != null) {
                        taskExt = taskOperService.getTaskExtByBpmnTask(hisTask.getProcessDefinitionId(), hisTask.getProcessInstanceId(), taskId, hisTask.getTaskDefinitionKey(), variableKeys);
                    }
                }
                if (taskExt != null) {
                    result.setResultCode(ResultEnum.SUCCESS.getValue());
                    result.setData(taskExt);
                } else {
                    result.setResultCode(ResultEnum.FAIL.getValue());
                    result.setMessage("can not find any task!");
                }
            }
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "5.11 获取流程实例已经运行的最后一个节点的票数统计", position = 11)
    // @ApiOperationSupport(order = 11)
    @GetMapping(value = "/queryLastRunnedNodeVoteCount.json")
    @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true)
    public Result queryLastRunnedNodeVoteCount(String processInstanceId) {
        Result result = new Result();
        try {
            Map<String, Integer> resultMap = taskOperService.queryLastRunnedNodeVoteCount(processInstanceId);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
            result.setData(resultMap);
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "5.12 获取流程实例已经运行的最后一个节点的taskDefinitionKey", position = 12)
    // @ApiOperationSupport(order = 12)
    @GetMapping(value = "/queryLastRunnedNodeId.json")
    @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true)
    public Result queryLastRunnedNodeId(String processInstanceId) {
        Result result = new Result();
        try {
            String taskDefinitionKey = taskOperService.queryLastRunnedNodeId(processInstanceId);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
            result.setData(taskDefinitionKey);
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "5.7 根据用户批量更新审批人", position = 7)
    // @ApiOperationSupport(order = 7)
    @GetMapping(value = "/updateApproverBatch.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID"),
            @ApiImplicitParam(name = "originalUser", value = "原始用户"),
            @ApiImplicitParam(name = "replacedUser", value = "替换用户")
    })
    public Result updateApproverBatch(String tenantId, String originalUser, String replacedUser) {
        Result result = new Result();
        try {
            taskOperService.updateApproverBatch(tenantId, originalUser, replacedUser);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "5.13 修改待办审批人", position = 13)
    // @ApiOperationSupport(order = 13)
    @GetMapping(value = "/updateApprover.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID"),
            @ApiImplicitParam(name = "userId", value = "替换用户")
    })
    public Result updateApprover(String taskId, String userId) {
        Result result = new Result();
        try {
            taskOperService.updateApprover(taskId, userId);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }


}
