
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.TaskAgent;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.TaskSearch;

import java.util.List;

/**
 * 任务代理服务类
 */
public interface TaskAgentService {
    /**
     * 设置代理
     *
     * @param taskSearch
     * @throws UnitedException
     */
    void setAgent(String taskId, String operUser, String principal, String agent, String reason) throws UnitedException;

    /**
     * 获取用户代理的任务列表
     *
     * @param agent 代理用户id
     * @return
     * @throws UnitedException
     */
    List<TaskAgent> getMyAgentTasks(String agent) throws UnitedException;

    TaskAgent getAgentTask(String taskId);

    /**
     * 根据查询条件获取代理任务
     *
     * @param taskSearch
     * @return
     */
    List<TaskAgent> findAgentInfo(TaskSearch taskSearch);

    void finishTask(String taskId, String assignee);

    /**
     * 转办
     *
     * @param taskSearch
     * @throws UnitedException
     */
    void setAssignee(String taskId, String operUser, String inheritor, String reason) throws UnitedException;
}
