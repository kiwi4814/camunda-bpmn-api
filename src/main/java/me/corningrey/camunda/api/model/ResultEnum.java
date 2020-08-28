
package me.corningrey.camunda.api.model;

public enum ResultEnum {
    /**
     * 成功
     */
    SUCCESS("1", "成功"),
    /**
     * 失败
     */
    FAIL("0", "失败"),
    /**
     * 错误
     */
    ERROR("2", "错误");

    private String value;
    private String text;

    private ResultEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

}