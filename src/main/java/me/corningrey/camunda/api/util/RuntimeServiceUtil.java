package me.corningrey.camunda.api.util;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

/**
 * Created by shucheng on 2019-7-12 下午 14:54
 */
public class RuntimeServiceUtil extends ServiceUtil {

    public static ProcessInstanceModificationBuilder getProcessInstanceModificationByInstId(String processInstanceId) {
        return getRuntimeService().createProcessInstanceModification(processInstanceId);
    }

    /**
     * 根据流程实例id查询单个流程实例
     *
     * @param processInstanceId
     * @return
     */
    public static ProcessInstance queryProcessInstanceById(String processInstanceId) {
        return getProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 判断流程实例是否处于暂停状态
     *
     * @param processInstanceId
     * @return
     */
    public static boolean isSuspended(String processInstanceId) {
        return ServiceUtil.getProcessInstanceQuery().processInstanceId(processInstanceId).active().suspended().count() == 1;
    }

    /**
     * 根据流程定义id查询单个流程定义
     *
     * @param processDefinitionId
     * @return
     */
    public static ProcessDefinition findProcessDefinitionById(String processDefinitionId) {
        return getProcessDefinitionQuery().processDefinitionId(processDefinitionId)
                .singleResult();
    }

    /**
     * 流程跳转连线
     *
     * @param processInstanceId 流程实例id
     * @param sequenceId        连线id
     * @param currentActivityId 获取当前task所在的activityId（taskDefinitionKey）
     */
    public static void turnLine(String processInstanceId, String sequenceId, String currentActivityId) {
        getProcessInstanceModificationByInstId(processInstanceId)
                .startTransition(sequenceId)
                .cancelAllForActivity(currentActivityId)
                .execute(true, true);
    }
}
