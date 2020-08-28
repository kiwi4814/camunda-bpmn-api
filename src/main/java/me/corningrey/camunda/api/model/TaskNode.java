package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskNode {
    /**
     * 定义Key
     */
    private String id;

    /**
     * 定义名称
     */
    private String name;

    /**
     * 审批人类型
     */
    private String approverType;

    /**
     * 审批人
     */
    private String approvers;

    /**
     * 下一个节点（仅适用于两个串行的节点）
     */
    private TaskNode nextNode;

    /**
     * 返回该节点前面的顺序流ID：多条线仅返回其中一条
     */
    private String incoming;

}
