package me.corningrey.camunda.api.util;

import org.camunda.bpm.engine.history.*;

/**
 * Created by shucheng on 2019-7-12 下午 15:25
 */
public class HistoryServiceUtil extends ServiceUtil {

    // 根据待办id获取历史任务实例
    public static HistoricTaskInstance getHistoricTaskInstanceByTaskId(String taskId) {

        HistoricTaskInstanceQuery htiq = getHistoricTaskInstanceQuery();
        return htiq.taskId(taskId).singleResult();
    }

    // 根据查询器和变量名获取历史变量实例
    public static HistoricVariableInstance getHisVarInstByQueryName(HistoricVariableInstanceQuery historicVariableInstanceQuery,
                                                                    String variableName) {
        return historicVariableInstanceQuery.variableName(variableName).singleResult();
    }

    public static HistoricProcessInstance getHistoricProcessInstanceByInstanceId(String instanceId) {
        HistoricProcessInstanceQuery hpiq = getHistoricProcessInstanceQuery();
        return hpiq.processInstanceId(instanceId).singleResult();
    }
}
