
package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表单变量对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormVariable implements Serializable {
    private static final long serialVersionUID = -1440417740971530955L;
    /**
     * 变量名称
     */

    private String name;

    /**
     * 变量code
     */
    private String key;

    /**
     * 变量类型
     */
    private String type;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 数据类型
     */
    private String varType;
}
