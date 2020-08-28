
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
@ApiModel(value = "ProcessDoploy", description = "部署定义对象")
public class ProcessDoploy {
    /**
     * 流程部署的部署名称，默认为空
     */
    @ApiModelProperty(value = "部署名称", name = "deployName", required = true, dataType = "String")
    private String deployName;
    /**
     * 流程部署的资源名称，必须以bpmn20.xml或者bpmn结尾
     */
    @ApiModelProperty(value = "资源名称，必须以bpmn20.xml或者bpmn结尾", name = "resourceName", required = true, dataType = "String")
    private String resourceName;
    /**
     * 此流程的租户ID
     */
    @ApiModelProperty(value = "此流程的租户ID", name = "tenant", required = false, dataType = "String")
    private String tenant;
    /**
     * 流程部署的用户ID
     */
    @ApiModelProperty(value = "流程部署的用户ID", name = "currentUser", required = false, dataType = "String")
    private String currentUser;
    /**
     * BPMN文件在resource下的相对路径
     */
    @ApiModelProperty(value = "BPMN文件在resource下的相对路径", name = "resourcePath", required = false, dataType = "String")
    private String resourcePath;
    /**
     * BPMN文件的绝对路径
     */
    @ApiModelProperty(value = "BPMN文件的绝对路径", name = "filePath", required = false, dataType = "String")
    private String filePath;
}
