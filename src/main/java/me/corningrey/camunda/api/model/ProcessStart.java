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
@ApiModel(value = "ProcessSearch", description = "流程发起")
public class ProcessStart {

    /**
     * 流程定义ID
     */
    @ApiModelProperty(value = "流程定义ID", name = "processDefinitionId", dataType = "String")
    private String processDefinitionId;

    /**
     * 流程定义key
     */
    @ApiModelProperty(value = "流程定义key", name = "processDefinitionKey", dataType = "String")
    private String processDefinitionKey;
    
    /**
     * 流程实例名称
     */
    @ApiModelProperty(value = "流程实例名称", name = "instanceRemark", dataType = "String")
    private String instanceRemark;

    /**
     * 业务key
     */
    @ApiModelProperty(value = "业务key", name = "businessKey", dataType = "String")
    private String businessKey;

    /**
     * 申请人id
     */
    @ApiModelProperty(value = "申请人id", name = "applicant", dataType = "String")
    private String applicant;

    /**
     * 自选审批人
     */
    @ApiModelProperty(value = "自选审批人", name = "optionalUsers", dataType = "String")
    private String optionalUsers;

    @ApiModelProperty(value = "参数", name = "variables", dataType = "String")
    private String variables;
}
