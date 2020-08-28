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
@ApiModel(value = "VariableExt", description = "流程变量对象")
public class VariableExt {

    /**
     * 流程变量ID
     */
    @ApiModelProperty(value = "流程变量ID", name = "id", dataType = "String")
    private String id;

    /**
     * 流程变量名
     */
    @ApiModelProperty(value = "流程变量名", name = "name", dataType = "String")
    private String name;

    /**
     * 流程变量类型的名字（如：string，integer等）
     */
    @ApiModelProperty(value = "流程变量类型的名字（如：string，integer等）", name = "typeName", dataType = "String")
    private String typeName;

    /**
     * 流程变量的值
     */
    @ApiModelProperty(value = "流程变量的值", name = "value", dataType = "Object")
    private Object value;
}
