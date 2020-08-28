package me.corningrey.camunda.api.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum ActionEnum {
    pass("pass", "同意"),
    notPass("notPass", "不同意"),
    conditionalPass("conditionalPass", "有条件同意"),
    abstain("abstain", "弃权"),
    affirm("affirm", "确认"),
    communicate("communicate", "沟通"),
    endCommunicate("endCommunicate", "结束沟通");

    ActionEnum(String code, String name) {
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

    public static boolean isLegalAction(String actionCode) {
        for (int i = 0; i < ActionEnum.values().length; i++) {
            if (StringUtils.equals(actionCode, ActionEnum.values()[i].code)) {
                return true;
            }
        }
        return false;
    }

    // 根据code获取名称
    public static String getNameByCode(String code) {
        for (ActionEnum a : ActionEnum.values()) {
            if (code.equals(a.getCode())) {
                return a.getName();
            }
        }
        return "";
    }

    // 获取ActionEnum中的所有code
    public static List<String> getAllCode() {
        List<String> resultList = new ArrayList<>();
        ActionEnum[] actionEnums = ActionEnum.values();
        for (ActionEnum actionEnum : actionEnums) {
            resultList.add(actionEnum.getCode());
        }
        return resultList;
    }

    // 将传来的以逗号分隔的审批选项转换为map（key是审批选项code；value是审批选项名称）
    public static Map<String, String> convertCodeToMap(String commaCodeStr) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(commaCodeStr)) {
            List<String> codeList = new ArrayList<>(Arrays.asList(commaCodeStr.split(",")));
            for (String actionCode : codeList) {
                if (isLegalAction(actionCode)) {
                    resultMap.put(actionCode, getNameByCode(actionCode));
                }
            }
        }
        return resultMap;
    }
}
