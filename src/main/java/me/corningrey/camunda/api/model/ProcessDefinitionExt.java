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
@ApiModel(value = "ProcessDefinitionExt", description = "流程定义对象")
public class ProcessDefinitionExt {

    /**
     * 流程定义id
     */
    @ApiModelProperty(value = "流程定义id", name = "id", dataType = "String")
    private String id;
    /**
     * 流程定义key
     */
    @ApiModelProperty(value = "流程定义key", name = "key", dataType = "String")
    private String key;
    /**
     * 流程定义名称
     */
    @ApiModelProperty(value = "流程定义名称", name = "name", dataType = "String")
    private String name;
    /**
     * 版本
     */
    @ApiModelProperty(value = "版本", name = "version", dataType = "int")
    private int version;
    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", name = "category", dataType = "String")
    private String category;
    /**
     * 流程部署id
     */
    @ApiModelProperty(value = "流程部署id", name = "deploymentId", dataType = "String")
    private String deploymentId;
    /**
     * 部署资源名称
     */
    @ApiModelProperty(value = "部署资源名称", name = "resourceName", dataType = "String")
    private String resourceName;
    /**
     * 历史级别
     */
    @ApiModelProperty(value = "历史级别", name = "historyLevel", dataType = "Integer")
    private Integer historyLevel;
    /**
     * 流程图资源名称
     */
    @ApiModelProperty(value = "流程图资源名称", name = "diagramResourceName", dataType = "String")
    private String diagramResourceName;
    /**
     * 租户id
     */
    @ApiModelProperty(value = "租户id", name = "tenantId", dataType = "String")
    private String tenantId;

    /**
     * 流程自定义类型
     */
    @ApiModelProperty(value = "流程自定义类型", name = "processType", dataType = "String")
    private String processType;

    /**
     * 流程发起表单地址
     */
    @ApiModelProperty(value = "流程发起表单地址", name = "formUrl", dataType = "String")
    private String formUrl;

    /**
     * 流程查看表单地址
     */
    @ApiModelProperty(value = "流程查看表单地址", name = "viewUrl", dataType = "String")
    private String viewUrl;

    /**
     * 流程定义对应的已激活实例的数量
     */
    @ApiModelProperty(value = "流程定义对应的已激活实例的数量", name = "instanceCount", dataType = "Integer")
    private Integer instanceCount;

}
