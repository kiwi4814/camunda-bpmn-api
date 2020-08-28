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
public class TaskAPISearch {

    @ApiModelProperty(value = "租户ID", required = true)
    private String tenantId;

    @ApiModelProperty(value = "审批人ID")
    private String userId;

    @ApiModelProperty(value = "流程定义Key")
    private String processDefinitionKey;

    @ApiModelProperty(value = "流程定义名称（模糊查询）")
    private String processDefinitionName;

    @ApiModelProperty(value = "任务Key")
    private String taskDefinitionKey;

    @ApiModelProperty(value = "任务节点名称（模糊查询）")
    private String taskDefinitionName;

    @ApiModelProperty(value = "业务主体key")
    private String businessKey;

    @ApiModelProperty(value = "流程实例ID")
    private String instanceId;

    @ApiModelProperty(value = "待办完成状态(0为已完成，1为未完成，默认查询全部)")
    private String finishedStatus;

    @ApiModelProperty(value = "流程实例完成状态(0为已完成，1为未完成，默认查询全部)")
    private String instanceStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    @ApiModelProperty(value = "每页几条数据")
    private Integer pageLimit;

    @ApiModelProperty(value = "排序字段")
    private String orderString;

    @ApiModelProperty(value = "变量名称查询，支持传多个以逗号分隔，返回值统一封装在variableMap中")
    private String variableKeys;

    @ApiModelProperty(value = "筛选条件，模糊筛选流程变量（JSON格式）")
    private String variableLike;

    @ApiModelProperty(value = "变量名称，精确筛选流程变量（JSON格式）")
    private String variableEquals;

    @ApiModelProperty(value = "变量名称，精确排除流程变量（JSON格式）")
    private String variableNotEquals;

    @ApiModelProperty(value = "待办打回状态（0为正常的待办，1为被打回的流程的待办，默认查询全部）")
    private String suspendStatus;

    @ApiModelProperty(value = "查询人（控制权限相关）")
    private String currentUser;

    @ApiModelProperty(value = "实例开始时间查询（开始时间）")
    private String instStartTimeStart;

    @ApiModelProperty(value = "实例开始时间查询（结束时间）")
    private String instStartTimeEnd;

    @ApiModelProperty(value = "实例结束时间查询（开始时间）")
    private String instEndTimeStart;

    @ApiModelProperty(value = "实例结束时间查询（结束时间）")
    private String instEndTimeEnd;

    @ApiModelProperty(value = "待办开始时间查询（开始时间）")
    private String taskStartTimeStart;

    @ApiModelProperty(value = "待办开始时间查询（结束时间）")
    private String taskStartTimeEnd;

    @ApiModelProperty(value = "查待办结束时间查询（开始时间）")
    private String taskEndTimeStart;

    @ApiModelProperty(value = "待办结束时间查询（结束时间）")
    private String taskEndTimeEnd;

    @ApiModelProperty(value = "只查询审批过的流程（此条件仅会筛选已完成的待办，例如因为流程跳转、多人审批一人同意而产生的其他待办不会被查询出来）")
    private Boolean onlyApproved;

}
