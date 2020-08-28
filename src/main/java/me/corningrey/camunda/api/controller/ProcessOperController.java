
package me.corningrey.camunda.api.controller;

import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.service.ProcessOperService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.util.RepositoryServiceUtil;
import me.corningrey.camunda.api.util.TaskServiceUtil;
import me.corningrey.camunda.api.model.Result;
import me.corningrey.camunda.api.model.UnitedLogger;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.BpmnVariableConstant;
import me.corningrey.camunda.api.model.CommonConstant;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 流程操作（撤回、打回、跳转）
 */

@RestController
@RequestMapping(value = "/api/process/")
@Api(value = "ProcessOperController", tags = {"「06」 流程控制相关接口（撤回、打回、跳转等）"})
public class ProcessOperController {
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ProcessOperService processOperService;
    @Resource
    private ProcessOperHistoryService processOperHistoryService;

    /**
     * 流程撤回，流程取消（流程删除）
     *
     * @param processInstanceId 流程实例id
     * @param reason            撤回原因
     */
    @ApiOperation(value = "6.1 流程撤回", position = 1)
    // @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "流程实例id", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作人", required = true),
            @ApiImplicitParam(name = "reason", value = "撤回原因", required = true)
    })
    @PostMapping(value = "/cancelProcess.json")
    public Result cancelProcess(@RequestParam(name = "processInstanceId", defaultValue = "") String processInstanceId,
                                @RequestParam(name = "operUser", defaultValue = "") String operUser,
                                @RequestParam(name = "reason", defaultValue = "") String reason) {
        Result result = new Result();

        // 参数验证
        if (StringUtils.isEmpty(processInstanceId)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程撤回失败：流程实例id为空！");
        } else if (StringUtils.isEmpty(operUser)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程撤回失败：操作人为空！");
        } else if (StringUtils.isEmpty(reason)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程撤回失败：撤回原因为空！");
        } else { // 验证通过
            runtimeService.deleteProcessInstance(processInstanceId, BpmnVariableConstant.SUFFIX_CANCEL_REASON, true, true);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
            result.setMessage("流程撤回成功");
            processOperHistoryService.insertCancelHistory(processInstanceId, operUser, reason);
        }
        return result;
    }

    /**
     * 流程打回，提供改流程状态的接口，发送通知（流程暂停）
     *
     * @param taskId         待办id
     * @param approveOpinion 打回原因
     */
    @ApiOperation(value = "6.2 流程打回", position = 2)
    // @ApiOperationSupport(order = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "待办id", required = true),
            @ApiImplicitParam(name = "approveOpinion", value = "打回原因")
    })
    @PostMapping(value = "/backProcess.json")
    public Result backProcess(@RequestParam(name = "taskId", defaultValue = "") String taskId,
                              @RequestParam(name = "approveOpinion", required = false) String approveOpinion) {
        Result result = new Result();

        // 参数验证
        if (StringUtils.isEmpty(taskId)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程打回失败：待办id为空！");
            return result;
        }
        if (StringUtils.isEmpty(approveOpinion)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程打回失败：打回原因为空！");
            return result;
        }

        // 查询待办对应的流程是否正在进行，如果正在进行，则获取其流程实例id
        String processInstanceId = null;
        try {
            processInstanceId = TaskServiceUtil.getTaskByTaskId(taskId).getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                result.setResultCode(ResultEnum.FAIL.getValue());
                result.setMessage("流程打回失败：该流程已经结束！");
                return result;
            } else {
                // 流程正在进行，查询该流程是否已经暂停
                if (processInstance.isSuspended()) {
                    result.setResultCode(ResultEnum.FAIL.getValue());
                    result.setMessage("流程打回失败：该流程已处于暂停状态！");
                    return result;
                }
            }
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程打回失败：流程查询失败！");
            return result;
        }

        // 流程打回操作
        runtimeService.suspendProcessInstanceById(processInstanceId);
        // 插入暂停日志
        processOperHistoryService.insertBackHistory(taskId, approveOpinion, CommonConstant.SUSPEND_TRUE);
        result.setResultCode(ResultEnum.SUCCESS.getValue());
        result.setMessage("流程打回成功");
        return result;
    }

    @ApiOperation(value = "6.3 流程恢复", position = 3)
    // @ApiOperationSupport(order = 3)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "流程实例id", required = true),
            @ApiImplicitParam(name = "approveOpinion", value = "恢复原因")
    })
    @PostMapping(value = "/restoreProcess.json")
    public Result restoreProcess(@RequestParam(name = "processInstanceId", defaultValue = "") String processInstanceId,
                                 @RequestParam(name = "approveOpinion", required = false) String approveOpinion) {
        Result result = new Result();

        // 参数验证
        if (StringUtils.isEmpty(processInstanceId)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程恢复失败：流程实例id为空！");
            return result;
        }

        // 查询待办对应的流程是否正在进行，如果正在进行，则获取其流程实例id
        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                result.setResultCode(ResultEnum.FAIL.getValue());
                result.setMessage("流程恢复失败：该流程已经结束！");
                return result;
            } else {
                // 流程正在进行，查询该流程是否已经暂停
                if (!processInstance.isSuspended()) {
                    result.setResultCode(ResultEnum.FAIL.getValue());
                    result.setMessage("流程恢复失败：该流程已处于运行状态！");
                    return result;
                }
            }
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程恢复失败：流程查询失败！");
            return result;
        }

        // 执行流程恢复操作
        runtimeService.activateProcessInstanceById(processInstanceId);
        // 插入暂停或恢复日志
        // 查询需要恢复的流程实例id对应的待办id
        String taskId = processOperService.queryBackProcessTaskId(processInstanceId);
        processOperHistoryService.insertBackHistory(taskId, approveOpinion, CommonConstant.SUSPEND_FALSE);
        result.setResultCode(ResultEnum.SUCCESS.getValue());
        result.setMessage("流程恢复成功");
        return result;
    }

    /**
     * 流程跳转连线
     *
     * @param taskId     当前待办id
     * @param sequenceId 目标节点id（如果要跳转到最后一个节点，就传END）
     */
    @ApiOperation(value = "6.4 流程跳转连线", position = 4)
    // @ApiOperationSupport(order = 4)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "当前待办id", required = true),
            @ApiImplicitParam(name = "sequenceId", value = "连线id", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作人", required = true),
            @ApiImplicitParam(name = "operReason", value = "操作原因")
    })
    @PostMapping(value = "/turnLineProcess.json")
    public Result turnLineProcess(@RequestParam(name = "taskId", defaultValue = "") String taskId,
                                  @RequestParam(name = "sequenceId", defaultValue = "") String sequenceId,
                                  @RequestParam(name = "operUser", defaultValue = "") String operUser,
                                  @RequestParam(name = "operReason", defaultValue = "") String operReason) {
        Result result = new Result();

        try {
            processOperService.turnLine(taskId, sequenceId, operUser, operReason);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }

        return result;
    }

    /**
     * 查询流程的所有连线信息
     *
     * @param processDefinitionId 流程定义id
     */
    @ApiOperation(value = "6.6 查询流程的所有连线信息【推荐使用1.4】", position = 6)
    // @ApiOperationSupport(order = 6)
    @GetMapping(value = "/queryAllSequences.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID", required = true),
            @ApiImplicitParam(name = "pageNum", value = "页码"),
            @ApiImplicitParam(name = "pageLimit", value = "每页数量")
    })
    public Result queryAllSequences(String processDefinitionId, Integer pageNum, Integer pageLimit) {
        Result result = new Result();
        try {
            List<Map<String, String>> sequenceList = RepositoryServiceUtil.queryAllSequences(processDefinitionId);
            result.setData(
                    CamundaUtil.getSimpleParamer(sequenceList, pageNum, pageLimit));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage(e.getMessage());
            UnitedLogger.error(e.getMessage());
        }
        return result;
    }

    /**
     * 批量结束流程
     *
     * @param processInstanceIds 如果有多个流程实例id，以逗号分隔
     * @param operUser           操作人
     */
    @ApiOperation(value = "6.7 批量结束流程【推荐使用3.6】", position = 7)
    // @ApiOperationSupport(order = 7)
    @PostMapping(value = "/oneKeyFinish.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceIds", value = "流程实例ID(多个用逗号拼接)", required = true),
            @ApiImplicitParam(name = "operUser", value = "操作用户", required = true),
            @ApiImplicitParam(name = "operReason", value = "操作原因")
    })
    public Result oneKeyFinish(String processInstanceIds, String operUser, String operReason) {

        Result result = new Result();
        processInstanceIds = CamundaUtil.decode(processInstanceIds, StandardCharsets.UTF_8.name());
        try {
            List<String> processInstanceList = Arrays.asList(processInstanceIds.split(","));
            processOperService.oneKeyFinish(processInstanceList, operUser, operReason);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }

        return result;
    }

    /**
     * 查询操作日志
     *
     * @param operHistorySearch 操作日志查询条件
     */
    @ApiOperation(value = "6.5 查询操作日志【推荐使用2.2】", position = 5)
    // @ApiOperationSupport(order = 5)
    @GetMapping(value = "/findProcessOperHistoryList.json")
    public Result findProcessOperHistoryList(@ModelAttribute("operHistorySearch") OperHistorySearch operHistorySearch) {
        Result result = new Result();

        try {
            List<ProcessOperHistory> list = processOperHistoryService.findProcessOperHistory(operHistorySearch);
            result.setData(
                    CamundaUtil.getSimpleParamer(list, operHistorySearch.getPageNum(), operHistorySearch.getPageLimit()));
            result.setResultCode(ResultEnum.SUCCESS.getValue());
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }
        return result;
    }

    /**
     * 删除流程实例数据以及操作日志
     *
     * @param tenantId      租户id
     * @param definitionKey 流程定义key
     */
    @ApiOperation(value = "6.8 删除指定租户下，特定definitionKey的流程实例及其操作日志", position = 8)
    // @ApiOperationSupport(order = 8)
    @PostMapping(value = "/deleteProcessInstanceAndOperHis.json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID"),
            @ApiImplicitParam(name = "definitionKey", value = "流程定义key")
    })
    public Result deleteProcessInstanceAndOperHis(@RequestParam(name = "tenantId", defaultValue = "") String tenantId,
                                                  @RequestParam(name = "definitionKey", defaultValue = "") String definitionKey) {
        Result result = new Result();

        // 参数校验
        if (StringUtils.isEmpty(tenantId)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程删除失败：租户id为空！");
            return result;
        } else if (StringUtils.isEmpty(definitionKey)) {
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessage("流程删除失败：definitionKey为空！");
            return result;
        }

        try {
            processOperService.deleteProcessInstanceAndOperHis(tenantId, definitionKey);
            result.setResultCode(ResultEnum.SUCCESS.getValue());
            result.setMessage("流程删除成功");
        } catch (Exception e) {
            UnitedLogger.error(e);
            result.setResultCode(ResultEnum.FAIL.getValue());
            result.setMessageCode(e.getMessage());
        }
        return result;
    }
}
