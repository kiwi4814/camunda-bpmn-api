package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeTask {
    /**
     * 传阅待办ID
     */
    private String id;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 发送传阅的待办ID
     */
    private String taskId;
    /**
     * 发送传阅的操作人
     */
    private String operUser;
    /**
     * 传阅审批人
     */
    private String assignee;
    /**
     * 传阅待办的创建时间
     */
    private String createTime;
    /**
     * 传阅待办的结束时间
     */
    private String endTime;
    /**
     * 针对传阅的描述
     */
    private String description;
    /**
     * 传阅人审批时候的意见
     */
    private String comment;
}
