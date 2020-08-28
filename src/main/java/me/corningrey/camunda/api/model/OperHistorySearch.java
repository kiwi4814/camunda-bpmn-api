
package me.corningrey.camunda.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作历史查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "OperHistorySearch", description = "操作历史查询对象")
public class OperHistorySearch {

    /**
     * 租户id
     */
    @ApiModelProperty(value = "租户ID", required = true)
    private String tenantId;

    /**
     * 流程实例id
     */
    @ApiModelProperty(value = "流程实例id")
    private String processInstanceId;

    /**
     * 任务Key
     */
    @ApiModelProperty(value = "任务Key")
    private String taskDefinitionKey;

    /**
     * 操作人ID
     */
    @ApiModelProperty(value = "操作人ID")
    private String operUser;

    /**
     * 是否查询还未执行的操作
     */
    @ApiModelProperty(value = "是否查询未执行的操作（1：是），默认否")
    private String operStatus;

    /**
     * 页码（调接口要传的）
     */
    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    /**
     * 每页几条数据（调接口要传的）
     */
    @ApiModelProperty(value = "每页几条数据")
    private Integer pageLimit;

    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段")
    private String orderString;

    /**
     * 自定义变量查询
     */
    @ApiModelProperty(value = "变量名称，支持传多个以逗号分隔")
    private String variableKeys;

    /**
     * 模糊匹配自定义变量
     */
    @ApiModelProperty(value = "筛选条件，模糊查询流程变量（JSON格式）")
    private String variableLike;

    /**
     * 精确匹配自定义变量
     */
    @ApiModelProperty(value = "变量名称，精确查询流程变量（JSON格式）")
    private String variables;

    /**
     * 查询人（控制权限）
     */
    @ApiModelProperty(value = "查询人（控制权限相关）")
    private String currentUser;

    /**
     * 操作类型，多个用逗号拼接
     */
    @ApiModelProperty(value = "操作类型，多个用逗号拼接")
    private String operTypes;

}
