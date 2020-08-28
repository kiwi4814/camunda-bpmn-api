
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.ActivityExt;
import me.corningrey.camunda.api.model.ActivitySearch;

import java.util.List;
import java.util.Map;


public interface ProcessInstanceService {

    List<ActivityExt> findHistoricActivityList(ActivitySearch activitySearch) throws UnitedException;

    /**
     * 在发起一个流程或者审批一个待办之后返回流程实例的最新状态
     *
     * @param instanceId 流程实例ID
     * @param taskId     待办ID
     * @return Map<String, String>
     * =========> processInstanceId: 流程实例的ID
     * =========> instanceState: 流程实例的状态（ACTIVE,COMPLETED...）
     * =========> currentTaskDefKey: 当前最新的节点的Key（多个逗号拼接）
     * =========> isUpdate: 发起或者审批前后，流程的进度是否有更新（例如节点1跳到节点2）
     */
    Map<String, String> findProcessInstanceStatus(String instanceId, String taskId) throws UnitedException;

    /**
     * 查询指定租户下，审批人关联的流程实例id
     *
     * @param tenantId
     * @param assignee
     * @return
     */
    List<String> findInstanceIdsByAssignee(String tenantId, String assignee);

    /**
     * 执行流程跳转操作
     *
     * @param instanceId
     * @param activityId
     * @param variableMap
     * @param isAfter
     * @throws Exception
     */
    void executeProcessModification(String instanceId, String activityId, String variableMap, String isAfter, String operUser, String operType, String reason) throws Exception;

    /**
     * 恢复被打回/暂停的流程并且跳转到开始节点
     *
     * @param instanceId
     * @param variableMap
     * @throws Exception
     */
    void activeAndModifyToStart(String instanceId, String variableMap, String operUser, String operType, String reason) throws Exception;

    /**
     * 根据流程实例ID查询流程定义ID
     *
     * @param instanceId
     * @return
     */
    String findProcessDefinitionByInstance(String instanceId);
}
