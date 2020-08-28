
package me.corningrey.camunda.api.service;


import java.util.List;

public interface TaskSignService {
    /**
     * 在当前运行中的节点上加签，影响计票结果
     *
     * @param taskId    任务ID
     * @param signUsers 加签用户，多个用户逗号拼接
     */
    List<String> runningPlusSign(String taskId, String signUsers);

    /**
     * 加签接口V1.2
     *
     * @param taskId    操作的待办ID
     * @param signUsers 加签用户（逗号拼接）
     * @param reason    原因
     * @param operUser  操作人，默认为当前待办的审批人
     */
    List<String> manualSignTasks(String taskId, String signUsers, String reason, String operUser);


}
