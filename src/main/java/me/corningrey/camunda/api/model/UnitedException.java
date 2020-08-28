
package me.corningrey.camunda.api.model;

/**
 * UnitedException：通用自定义异常管理
 *
 * @Action 所有应用程序的异常管理全部使用该类，注意log等级
 * @Description loadErrorMessageByCode的方法支持后台异常国际化转化
 */
@SuppressWarnings("serial")
public class UnitedException extends Exception {
    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误参数名
     */
    private String errorParam;

    public UnitedException(Throwable cause) {
        super(cause);
    }

    public UnitedException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public UnitedException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public UnitedException(String errorParam, String errorCode) {
        super(errorCode);
        this.errorParam = errorParam;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorParam() {
        return errorParam;
    }

}
