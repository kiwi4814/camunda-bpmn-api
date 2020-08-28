package me.corningrey.camunda.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@ApiModel(value = "PageResult", description = "分页查询返回对象")
public class PageResult<T> {

    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码")
    private Integer pageNum;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    private Integer pageLimit;

    /**
     * 数据总条数
     */
    @ApiModelProperty(value = "数据总条数")
    private Long total;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数")
    private Integer pageCount;

    /**
     * 是否含有下一页
     */
    @ApiModelProperty(value = "是否含有下一页")
    private Boolean isHasNextPage;

    /**
     * 数据体
     */
    @ApiModelProperty(value = "数据体列表")
    private List<T> list;
}
