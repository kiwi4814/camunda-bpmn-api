package me.corningrey.camunda.api.service;

public interface NoticeTaskService {

    /**
     * 传阅接口
     * @param taskId 操作的待办id
     * @param description 针对传阅的描述
     * @param assignees 传阅审批人(逗号拼接)
     * @param operUser 操作人
     */
    void insertNoticeTask(String taskId, String description,String assignees, String operUser);

    /**
     * 审批传阅接口
     * @param id 传阅id
     * @param operUser 操作人
     * @param reason  传阅人审批时候的意见
     */
    void approvalNoticeTask(String id,String operUser, String reason);
    /**
     * 传阅修改审批人接口
     * @param id 传阅id
     * @param operUser 操作人
     * @param assignee  传阅人ID
     */
    void updateAssigneeNoticeTask(String id,String operUser, String assignee);
    /**
     * 删除传阅接口
     * @param id 传阅id
     * @param operUser 操作人
     */
    void deletAssigneeNoticeTask(String id,String operUser);
}
