
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.TaskExt;
import me.corningrey.camunda.api.model.TaskSearch;

import java.util.List;
import java.util.Map;

/**
 * 任务服务类
 */
public interface TaskOperService {
    /**
     * 查询所有任务（正在进行+已完成）
     *
     * @param taskSearch 目前能用的查询条件为流程实例id（instanceId），审批人（userId）
     * @return
     * @throws UnitedException
     */
    Map<String, Object> findTasks(TaskSearch taskSearch) throws UnitedException;

    /**
     * 根据用户id查询所有待办任务
     *
     * @param taskSearch 目前能用的查询条件为流程实例id（instanceId），审批人（userId）
     *                   审批人userId为空，就查所有审批人的
     * @return
     */
    List<TaskExt> findToDoTasks(TaskSearch taskSearch) throws UnitedException;

    TaskExt getTaskExtByBpmnTask(String processDefinitionId, String processInstanceId, String taskId, String taskDefinitionKey, String variableKeys);

    /**
     * @param taskId          任务ID
     * @param assignee        审批人
     * @param action          审批结果，可以为空
     * @param comment         审批意见，可以为空
     * @param completionLevel 完成条件校验的等级
     * @param variables       自定义变量，可以为空
     * @throws UnitedException
     */
    void audit(String taskId, String assignee, String action, String comment,
               Integer completionLevel, String optionalUsers, Map<String, Object> variables) throws UnitedException;

    /**
     * 获取指定流程已经运行的最后一个节点的票数统计
     *
     * @param processInstanceId
     * @return
     */
    Map<String, Integer> queryLastRunnedNodeVoteCount(String processInstanceId) throws UnitedException;

    /**
     * 获取指定流程已经运行的最后一个节点的taskDefinitionKey
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    String queryLastRunnedNodeId(String processInstanceId) throws Exception;

    /**
     * 获取指定节点上的票数统计
     *
     * @param taskId
     * @param taskDefinitionKey
     * @return
     */
    Map<String, Integer> getAllCountByTaskDefinitionKey(String taskId, String taskDefinitionKey);

    /**
     * 更改某个租户下的未完成待办的审批人
     *
     * @param tenantId     租户ID
     * @param originalUser 原始用户
     * @param replacedUser 替换用户
     */
    void updateApproverBatch(String tenantId, String originalUser, String replacedUser) throws UnitedException;

    /**
     * 根据taskId更新单个审批人
     *
     * @param taskId   任务ID
     * @param approver 审批人ID
     */
    void updateApprover(String taskId, String approver) throws UnitedException;

}
