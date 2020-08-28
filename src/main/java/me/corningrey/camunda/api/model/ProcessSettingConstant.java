package me.corningrey.camunda.api.model;

public class ProcessSettingConstant {
    //方法类型
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    //审批人类型
    public static final String USERTYPE_USER = "1"; // 审批人类型--用户
    public static final String USERTYPE_TEAM = "2"; // 审批人类型--团队
    public static final String USERTYPE_ROLE = "3"; // 审批人类型--角色
    public static final String USERTYPE_DEPA = "4"; // 审批人类型--部门
    public static final String GROUPTYPE = "2,3,4"; // 用户组类型的合集

    // 接口类型
    public static final String INTERFACE_DATA = "101"; // 数据接口
    public static final String INTERFACE_SERVICE = "102"; // 服务接口
}
