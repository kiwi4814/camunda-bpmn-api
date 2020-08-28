
package me.corningrey.camunda.api.service;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.util.List;
import java.util.Map;


public interface ProcessOperService {


    /**
     * 流程转向操作
     * @param taskId
     * @param activityId
     * @param operUser
     */
    void turnProcess(String taskId, String activityId, String operUser,String reason) throws Exception;

    /**
     * 跳转连线操作
     * @param taskId
     * @param sequenceId
     * @param operUser
     * @throws Exception
     */
    void turnLine(String taskId, String sequenceId, String operUser,String reason) throws Exception;

    /**
     * 流程一键结束（单个）
     * @param processInstanceId
     * @param operUser
     * @throws Exception
     */
    void oneKeyFinish(String processInstanceId, String operUser,String reason) throws Exception;

    /**
     * 流程一键结束（多个）
     * @param processInstanceIdList
     * @param operUser
     * @throws Exception
     */
    void oneKeyFinish(List<String> processInstanceIdList, String operUser,String reason) throws Exception;

    /**
     * 清空指定活动节点流向
     *
     * @param activityImpl
     *            活动节点
     * @return 节点流向集合
     */
    List<PvmTransition> clearTransition(ActivityImpl activityImpl);

    /**
     * 根据任务ID和节点ID获取活动节点 <br>
     *
     * @param taskId
     *            任务ID
     * @param activityId
     *            活动节点ID <br>
     *            如果为null或""，则默认查询当前活动节点 <br>
     *            如果为"end"，则查询结束节点 <br>
     *
     * @return
     * @throws Exception
     */
    ActivityImpl findActivitiImpl(String taskId, String activityId)
            throws Exception;

    /**
     * 还原指定活动节点流向
     *
     * @param activityImpl
     *            活动节点
     * @param oriPvmTransitionList
     *            原有节点流向集合
     */
    void restoreTransition(ActivityImpl activityImpl,
                           List<PvmTransition> oriPvmTransitionList);

    /**
     * 流程转向操作
     *
     * @param taskId
     *            当前任务ID
     * @param activityId
     *            目标节点任务ID
     * @param variables
     *            流程变量
     * @throws Exception
     */
    void turnTransition(String taskId, String activityId,
                        Map<String, Object> variables,String reason) throws Exception;

    /**
     * 根据任务ID获取流程定义
     *
     * @param taskId
     *            任务ID
     * @return
     * @throws Exception
     */
    ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(
            String taskId) throws Exception;

    /**
     * 根据任务id查询待办实体
     * @param taskId
     * @return
     * @throws Exception
     */
    TaskEntity findTaskById(String taskId) throws Exception;

    /**
     * 根据流程定义key删除相关的所有流程实例
     * @param definitionKey
     */
    void deleteProcessInstanceByDefKey(String definitionKey);

    /**
     * 删除指定租户下，特定definitionKey的流程实例及其操作日志
     * @param tenantId 租户id
     * @param definitionKey 流程定义key
     */
    void deleteProcessInstanceAndOperHis(String tenantId, String definitionKey);

    /**
     * 查询流程打回操作对应的待办id
     * @param processInstanceId 流程实例id
     * @return
     */
    String queryBackProcessTaskId(String processInstanceId);
}
