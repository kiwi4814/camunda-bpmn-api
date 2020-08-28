package me.corningrey.camunda.api.model;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TaskExt", description = " 待办任务结果对象")
public class TaskExt {
    /**
     * 任务的实例ID
     */
    @ApiModelProperty(value = "任务的实例ID")
    private String taskId;
    /**
     * 任务的审批人id
     */
    @ApiModelProperty(value = "任务的审批人id")
    private String assignee;
    /**
     * 申请人
     */
    @ApiModelProperty(value = "申请人ID")
    private String applyerId;
    /**
     * 任务的代理人id
     */
    @ApiModelProperty(value = "任务的代理人id")
    private String agent;
    /**
     * 任务的状态code（unfinished：未处理; finished：已处理; canceled 已取消）
     */
    @ApiModelProperty(value = "任务的状态code（unfinished：未处理; finished：已处理; canceled 已取消）")
    private String status;
    /**
     * 任务的处理结果code （pass 通过、notPass 否决）
     */
    @ApiModelProperty(value = "任务的处理结果code （pass 通过、notPass 否决）")
    private String result;
    /**
     * 任务的审批意见
     */
    @ApiModelProperty(value = "任务的审批意见")
    private String comment;
    /**
     * 任务的创建时间
     */
    @ApiModelProperty(value = "任务的创建时间")
    private Date startTime;
    /**
     * 任务的结束时间
     */
    @ApiModelProperty(value = "任务的结束时间")
    private Date endTime;
    /**
     * 任务对应的节点的定义Key
     */
    @ApiModelProperty(value = "任务对应的节点的定义Key")
    private String taskDefinitionKey;
    /**
     * 任务对应的节点的定义名称
     */
    @ApiModelProperty(value = "任务对应的节点的定义名称")
    private String taskDefinitionName;
    /**
     * 任务对应的节点的自定义设置ID
     */
    @ApiModelProperty(value = "任务对应的节点的自定义设置ID，1.2已废弃")
    @Deprecated
    private String taskSettingId;
    /**
     * 任务对应的节点的定义设置类型code
     */
    @ApiModelProperty(value = "任务对应的节点的定义设置类型code")
    private String taskSettingType;
    /**
     * 任务的审批选项
     */
    @ApiModelProperty(value = "任务的审批选项")
    private String approveOptions;
    /**
     * 节点的操作选项
     */
    @ApiModelProperty(value = "节点的操作选项")
    private String operActions;
    /**
     * 任务对应的实例ID
     */
    @ApiModelProperty(value = "任务对应的实例ID")
    private String instanceId;
    /**
     * 任务对应的执行分支ID
     */
    @ApiModelProperty(value = "任务对应的执行分支ID")
    private String executionId;
    /**
     * 任务对应的实例自定义名称
     */
    @ApiModelProperty(value = "任务对应的实例自定义名称")
    private String instanceRemark;
    /**
     * 任务对应的业务对象ID
     */
    @ApiModelProperty(value = "任务对应的业务对象ID")
    private String businessKey;
    /**
     * 任务对应的流程定义ID
     */
    @ApiModelProperty(value = "任务对应的流程定义ID")
    private String processDefinitionId;
    /**
     * 任务对应的流程定义KEY
     */
    @ApiModelProperty(value = "任务对应的流程定义KEY")
    private String processDefinitionKey;
    /**
     * 任务对应的流程定义名称
     */
    @ApiModelProperty(value = "任务对应的流程定义名称")
    private String processDefinitionName;
    /**
     * 流程变量Map
     */
    @ApiModelProperty(value = "流程变量Map")
    private JSONObject variableMap;
    /**
     * 流程发起时间
     */
    @ApiModelProperty(value = "流程发起时间")
    private Date processInstanceStartTime;

    /**
     * 当条待办是否为整个节点的最后一个待办
     */
    @ApiModelProperty(value = "当条待办是否为整个节点的最后一个待办（1为是）")
    private String isFinalApprover;

    /**
     * 待办可见性（0为可见，1为不可见）
     */
    @ApiModelProperty(value = "待办可见性（0为可见【即此条待办的审批人和代理人中至少有一个是自己】，1为不可见）")
    private String isVisible;
}
