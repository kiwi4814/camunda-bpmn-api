package me.corningrey.camunda.api.model;

import com.alibaba.fastjson.JSONObject;
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
@ApiModel(value = "ProcessOperHistory", description = "流程操作历史对象")
public class ProcessOperHistory {

    /**
     * 操作日志id
     */
    @ApiModelProperty(value = "操作日志id", name = "operHisId", dataType = "String")
    private Integer operHisId;
    /**
     * 流程名称
     */
    @ApiModelProperty(value = "流程名称", name = "processName", dataType = "String")
    private String processName;
    /**
     * 流程实例id
     */
    @ApiModelProperty(value = "流程实例id", name = "processInstanceId", dataType = "String")
    private String processInstanceId;
    /**
     * 流程实例名称
     */
    @ApiModelProperty(value = "流程实例名称", name = "processInstanceRemark", dataType = "String")
    private String processInstanceRemark;
    /**
     * 操作类型code
     */
    @ApiModelProperty(value = "操作类型code", name = "operType", dataType = "String")
    private String operType;
    /**
     * 操作类型名称
     */
    @ApiModelProperty(value = "操作类型名称", name = "operTypeName", dataType = "String")
    private String operTypeName;
    /**
     * 当前所处的待办id
     */
    @ApiModelProperty(value = "当前所处的待办id", name = "taskId", dataType = "String")
    private String taskId;
    /**
     * 任务Key
     */
    @ApiModelProperty(value = "任务Key", name = "taskDefinitionKey", dataType = "String")
    private String taskDefinitionKey;
    /**
     * 当前所处的节点名称
     */
    @ApiModelProperty(value = "当前所处的节点名称", name = "nodeName", dataType = "String")
    private String nodeName;
    /**
     * 当前操作人ID
     */
    @ApiModelProperty(value = "当前操作人ID", name = "operUser", dataType = "String")
    private String operUser;

    /**
     * 原因
     */
    @ApiModelProperty(value = "原因", name = "operReason", dataType = "String")
    private String operReason;

    /**
     * 操作时间
     */

    @ApiModelProperty(value = "操作时间", name = "operTime", dataType = "String")
    private String operTime;
    /**
     * 操作说明
     */
    @ApiModelProperty(value = "操作说明", name = "operComment", dataType = "String")
    private String operComment;

    /**
     * 操作说明
     */
    @ApiModelProperty(value = "流程变量Map", name = "variableMap", dataType = "json")
    private JSONObject variableMap;

    /**
     * 此条操作历史的可见性（0为可见，1为不可见）
     */
    @ApiModelProperty(value = "此条操作历史的可见性（0为可见【操作人是自己】，1为不可见）")
    private String isVisible;
}
