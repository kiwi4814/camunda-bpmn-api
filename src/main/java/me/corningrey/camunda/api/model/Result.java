
package me.corningrey.camunda.api.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Result:是一个通用的响应结果序列化对象
 *
 * @Action 所有用于返回一个结果对象，不需要指定结构化的场景
 * @Description Ex01:返回JSON对象<br>Ex02:返回用于判断是否的场景
 */
@NoArgsConstructor
@Data
public class Result<T> {
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String message;

    /**
     * 信息状态码
     */
    @ApiModelProperty(value = "信息状态码")
    private String messageCode;

    /**
     * 结果状态码
     */
    @ApiModelProperty(value = "结果状态码，1为成功，2为失败")
    private String resultCode;

    /**
     * 数据体
     */
    @ApiModelProperty(value = "数据体")
    private T data;

}