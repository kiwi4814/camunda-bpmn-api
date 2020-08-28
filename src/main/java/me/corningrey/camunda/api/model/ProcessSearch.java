package me.corningrey.camunda.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ProcessSearch", description = "查询流程实例")
public class ProcessSearch {

    @ApiModelProperty(value = "流程定义ID")
    private String processDefinitionId;

    @ApiModelProperty(value = "流程定义Key")
    private String processDefinitionKey;

    @ApiModelProperty(value = "流程定义名称")
    private String processDefinitionName;

    @ApiModelProperty(value = "流程定义类型，支持逗号拼接")
    private String processType;

    @ApiModelProperty(value = "申请人ID")
    private String applyerId;

    @ApiModelProperty(value = "申请人名称")
    private String applyerName;

    @ApiModelProperty(value = "审批人ID（支持传多个id，以逗号分隔）")
    private String assignee;

    @ApiModelProperty(value = "审批人名称")
    private String assigneeName;

    @ApiModelProperty(value = "业务key，模糊匹配")
    private String businessKey;

    @ApiModelProperty(value = "租户ID")
    private String tenancyId;

    @ApiModelProperty(value = "租户名称")
    private String tenancyName;

    @ApiModelProperty(value = "流程实例ID")
    private String instanceId;

    @ApiModelProperty(value = "流程实例名称")
    private String instName;

    @ApiModelProperty(value = "实例激活状态【0为运行中，1为暂停（被打回）】,覆盖state查询参数")
    private String activeStatus;

    @ApiModelProperty(value = "实例完成状态【0为已完成，1为未完成】")
    private String finishedStatus;

    @ApiModelProperty(value = "实例状态自定义查询，支持多个逗号拼接【ACTIVE,COMPLETED,SUSPENDED,INTERNALLY_TERMINATED,EXTERNALLY_TERMINATED】", notes = "覆盖实例激活状态")
    private String state;

    @ApiModelProperty(value = "流程删除原因")
    private String deleteReason;

    @ApiModelProperty(value = "是否只查询最新定义的流程(false为查看所有版本的流程定义，默认为true)")
    private Boolean isLastVersion;

    @ApiModelProperty(value = "自定义变量查询【模糊匹配】")
    private String variablesLike;

    @ApiModelProperty(value = "自定义变量查询【精确匹配】")
    private String variablesEquals;

    @ApiModelProperty(value = "列表中要查询的流程变量，多个变量逗号拼接，返回值统一放在variableMap中")
    private String variableKeys;

    @ApiModelProperty(value = "一次查询多个流程实例，格式为逗号拼接的流程实例ID")
    private String instanceIds;

    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    @ApiModelProperty(value = "每页几条数据")
    private Integer pageLimit;

    @ApiModelProperty(value = "排序字段")
    private String orderString;
}