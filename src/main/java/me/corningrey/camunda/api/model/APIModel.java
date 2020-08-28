
package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: APIModel
 * @Description: 接口
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class APIModel {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 租户ID
     */
    private String tenancyId;
    /**
     * 租户名称
     */
    private String tenancyName;
    /**
     * 接口名称
     */
    private String name;
    /**
     * URL
     */
    private String url;

    /**
     * 根据 ID 查询数据(非用户类型才有)
     */
    private String searchUrl;
    /**
     * 接口类型（数据类型 101，接口类型 102；ProcessSettingConstant）
     */
    private String interfaceType;
    /**
     * 类型
     */
    private String type;
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 请求方式
     */
    private String requestMethod;
    /**
     * 请求方式名称
     */
    private String requestMethodName;
    /**
     * 参数
     */
    private String parameter = "[]";
    /**
     * 是否默认
     */
    private String isDefault;
    /**
     * 备注
     */
    private String remark;

    private String orderString;

}
