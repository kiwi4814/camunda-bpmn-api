package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmnElement {
    /**
     * ID
     */
    String activityId;

    /**
     * 类型
     */
    String activityType;

    /**
     * 名称
     */
    String activityName;

    /**
     * 审批人EL（只有节点才会有）
     */
    String assigneeExp;

    /**
     * 完成条件EP
     */
    String completionExp;

    /**
     * 网关的所有输出流
     */
    List<BpmnElement> sequenceList;
}
