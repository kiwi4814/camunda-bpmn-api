package me.corningrey.camunda.api.enums;

import org.apache.commons.lang3.StringUtils;

public enum TaskStatusEnum {
    unfinished("unfinished","未完成"),
    suspended("suspended","已暂停"),
    finished("finished","已完成"),
    canceled("canceled","已取消");


    TaskStatusEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    private String name;
    private String code;
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static boolean isLegalStatus(String status){
        for (int i = 0; i < TaskStatusEnum.values().length; i++) {
            if(StringUtils.equals(status, TaskStatusEnum.values()[i].code)){
                return true;
            }
        }
        return false;
    }
}
