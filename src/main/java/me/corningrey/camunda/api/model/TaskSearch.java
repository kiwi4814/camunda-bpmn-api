package me.corningrey.camunda.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TaskSearch", description = "待办任务查询对象")
public class TaskSearch {

    /**
     * 审批人ID
     */
    @ApiModelProperty(value = "审批人ID", name = "userId", dataType = "String")
    private String userId;

    /**
     * 流程定义Key
     */
    @ApiModelProperty(value = "流程定义Key", name = "processDefinitionKey", dataType = "String")
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    @ApiModelProperty(value = "流程定义名称", name = "processDefinitionName", dataType = "String")
    private String processDefinitionName;

    /**
     * 业务主体key
     */
    @ApiModelProperty(value = "业务主体key", name = "businessKey", dataType = "String")
    private String businessKey;

    /**
     * 流程实例名称
     */
    @ApiModelProperty(value = "流程实例名称", name = "instanceRemark", dataType = "String")
    private String instanceRemark;

    /**
     * 流程实例id
     */
    @ApiModelProperty(value = "流程实例ID", name = "instanceId", dataType = "String")
    private String instanceId;

    /**
     * 任务Key
     */
    @ApiModelProperty(value = "任务Key", name = "taskDefinitionKey", dataType = "String")
    private String taskDefinitionKey;

    /**
     * 待办id
     */
    @ApiModelProperty(value = "待办任务ID", name = "taskId", dataType = "String")
    private String taskId;

    /**
     * 待办完成状态(0为已完成，1为未完成)
     */
    @ApiModelProperty(value = "待办完成状态(0为已完成，1为未完成)", name = "finishedStatus", dataType = "String")
    private String finishedStatus;

    /**
     * 待办完成状态(0为已完成，1为未完成)
     */
    @ApiModelProperty(value = "流程实例完成状态(0为已完成，1为未完成)", name = "instanceStatus", dataType = "String")
    private String instanceStatus;

    /**
     * 代理人ID
     */
    @ApiModelProperty(value = "代理人ID", name = "agent", dataType = "String")
    private String agent;
    /**
     * 被代理人ID
     */
    @ApiModelProperty(value = "被代理人ID", name = "principal", dataType = "String")
    private String principal;
    /**
     * 代理任务是否已经完成（y/n）
     */
    @ApiModelProperty(value = "代理任务是否已经完成(Y/N)", name = "isCompleted", dataType = "String")
    private String isCompleted;
    /**
     * 代理任务的最后执行人ID
     */
    @ApiModelProperty(value = "代理任务的最后执行人ID", name = "executor", dataType = "String")
    private String executor;
    /**
     * 操作人ID
     */
    @ApiModelProperty(value = "操作人ID", name = "operUser", dataType = "String")
    private String operUser;
    /**
     * 转办的新处理人ID
     */
    @ApiModelProperty(value = "转办的新处理人ID", name = "inheritor", dataType = "String")
    private String inheritor;
    /**
     * 租户id
     */
    @ApiModelProperty(value = "租户ID", name = "tenantId", dataType = "String")
    private String tenantId;
    /**
     * 是否查询还未执行的操作
     */
    @ApiModelProperty(value = "是否查询未执行的操作（0：否；1：是），默认否", name = "operStatus", dataType = "String")
    private String operStatus;
    /**
     * 页码（调接口要传的）
     */
    @ApiModelProperty(value = "页码", name = "pageNum", dataType = "Integer")
    private Integer pageNum;

    /**
     * 每页几条数据（调接口要传的）
     */
    @ApiModelProperty(value = "每页几条数据", name = "pageLimit", dataType = "Integer")
    private Integer pageLimit;

    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段", name = "orderString", dataType = "String")
    private String orderString;
    /**
     * 自定义变量查询
     */
    @ApiModelProperty(value = "变量名称，支持传多个以逗号分隔", name = "variableKeys", dataType = "String")
    private String variableKeys;

    /**
     * 自定义变量查询
     */
    @ApiModelProperty(value = "筛选条件，模糊查询流程变量", name = "variablesLike", dataType = "json")
    private String variableLike;

    /**
     * 自定义变量查询
     */
    @ApiModelProperty(value = "变量名称，精确查询流程变量", name = "variablesEquals", dataType = "json")
    private String variableEquals;
}
