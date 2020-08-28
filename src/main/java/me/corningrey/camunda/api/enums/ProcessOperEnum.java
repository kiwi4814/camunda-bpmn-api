package me.corningrey.camunda.api.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 流程操作类型enum
 */
public enum ProcessOperEnum {

    // 流程
    START("1", "发起"),
    SUSPEND("2", "打回"),
    ACTIVATE("3", "恢复"),
    TURN("4", "转向"),
    CANCEL("5", "撤销"),
    END("6", "结束"),

    // 待办
    AGENT("7", "代理"),
    DELIVER("8", "转办"),
    AUDIT("9", "审批"),
    APPEND("10", "加签"),
    CIRCULATE("11", "传阅"),
    AUDIT_SIGN("12", "审批（加签）");

    private String value;
    private String text;

    private ProcessOperEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static boolean isLegalAction(String actionCode) {
        for (int i = 0; i < ProcessOperEnum.values().length; i++) {
            if (StringUtils.equals(actionCode, ProcessOperEnum.values()[i].value)) {
                return true;
            }
        }
        return false;
    }

    // 根据code获取名称
    public static String getNameByCode(String code) {
        for (ProcessOperEnum a : ProcessOperEnum.values()) {
            if (code.equals(a.getValue())) {
                return a.getText();
            }
        }
        return "";
    }

    // 获取ProcessOperEnum中的所有code
    public static List<String> getAllCode() {
        List<String> resultList = new ArrayList<>();
        ProcessOperEnum[] actionEnums = ProcessOperEnum.values();
        for (ProcessOperEnum actionEnum : actionEnums) {
            resultList.add(actionEnum.getValue());
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
