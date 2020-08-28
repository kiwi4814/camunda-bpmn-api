package me.corningrey.camunda.api.util;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by shucheng on 2019-7-12 下午 14:49
 */
public class TaskServiceUtil extends ServiceUtil {

    // 根据待办id获取运行中的待办
    public static Task getTaskByTaskId(String taskId) {
        TaskQuery tq = getTaskQuery();
        return tq.taskId(taskId).singleResult();
    }

    /**
     * 根据任务id获取流程定义
     * @param taskId
     * @return
     */
    public static ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) {
        String processDefinitionId = getTaskByTaskId(taskId).getProcessDefinitionId();
        return (ProcessDefinitionEntity) getRepositoryService().getProcessDefinition(processDefinitionId);
    }

    // 根据待办id获取流程实例
    public static ProcessInstance findProcessInstanceByTaskId(String taskId) {
        String processInstanceId = getTaskByTaskId(taskId).getProcessInstanceId();
        return getProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    // 获取找人的service中拦截的下一节点的TaskDefinition
    public static TaskDefinition getNextTaskDefinitionInParsingService(DelegateExecution delegateExecution) {
        ExecutionEntity execution = (ExecutionEntity) delegateExecution;
        // 获取下一个节点的活动实例
        ActivityImpl activity = execution.getActivity().getActivities().get(0);
        UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
        return activityBehavior.getTaskDefinition();
    }

    // 获取找人的service中拦截的下一节点的节点名称
    public static String getNextNodeNameInParsingService(DelegateExecution delegateExecution) {
        TaskDefinition taskDefinition = getNextTaskDefinitionInParsingService(delegateExecution);
        return taskDefinition.getNameExpression().getExpressionText();
    }

    /**
     * 查询流程实例运行到的节点上的所有待办
     *
     * @param processInstanceId
     * @return
     */
    public static List<Task> queryRunnedTasksByInstanceId(String processInstanceId) {
        List<Task> taskList = new ArrayList<>();
        // 判断流程是否正在运行
        if (RuntimeServiceUtil.queryProcessInstanceById(processInstanceId) != null) {
            // 获取该流程实例正在执行的待办列表
            taskList = getTaskQuery().processInstanceId(processInstanceId).list();
        }
        return taskList;
    }

    /**
     * 查询流程实例运行到的节点上的待办（多条只返回其中一条）
     *
     * @param processInstanceId
     * @return
     */
    public static Task queryRunnedTaskByInstanceId(String processInstanceId) {
        Task task = null;
        List<Task> taskList = queryRunnedTasksByInstanceId(processInstanceId);
        if (CollUtil.isNotEmpty(taskList)) { // 流程中存在待办
            task = taskList.get(0);
        }
        return task;
    }

    /**
     * 根据待办id查询流程变量查询流程变量
     *
     * @param taskId       待办id
     * @param variableName 变量名称
     * @return
     */
    public static Object getVariable(String taskId, String variableName) {
        return getTaskService().getVariable(taskId, variableName);
    }

    /**
     * 查询节点待办总数
     *
     * @param taskId
     * @return
     */
    public static Integer queryTotalTaskCountByTaskId(String taskId) {
        return (Integer) getVariable(taskId, "nrOfInstances");
    }

    /**
     * 查询节点已完成待办数量
     *
     * @param taskId
     * @return
     */
    public static Integer queryCompletedTaskCountByTaskId(String taskId) {
        return (Integer) getVariable(taskId, "nrOfCompletedInstances");
    }

    /**
     * 检测正在进行的任务节点是否为多任务节点
     *
     * @param taskId
     * @return
     */
    public static boolean checkIsMultiTask(String taskId) {
        return queryTotalTaskCountByTaskId(taskId) != null;
    }

    // --------------------------------查询可驳回的节点start------------------------------------
    /**
     * 根据taskId查询该流程实例中所有可以驳回的节点集合（只取出UserTask类型的节点）
     * 即获取当前task对应节点之前的所有节点集合
     * @param taskId
     * @return
     */
    public static List<ActivityImpl> findBackActivity(String taskId) {
        ActivityImpl currActivity = findActivityImpl(taskId, null); // 获取当前taskId对应的activity
        List<ActivityImpl> resultList = iterateBackActivity(currActivity, new ArrayList<>(), new ArrayList<>());
        Collections.reverse(resultList);
        return resultList;
    }

    /**
     * 迭代循环流程树结构，查询当前节点可驳回的任务节点（真正递归操作的部分）
     * @param currActivity
     * @param rtnList
     * @param tempList
     * @return
     */
    public static List<ActivityImpl> iterateBackActivity(ActivityImpl currActivity, List<ActivityImpl> rtnList,
                                                         List<ActivityImpl> tempList) {
        // 当前节点的所有流入连线
        List<PvmTransition> incomingTransitions = currActivity.getIncomingTransitions();
        for (PvmTransition pvmTransition : incomingTransitions) {
            ActivityImpl activityImpl = (ActivityImpl) pvmTransition.getSource();
            if (!tempList.contains(activityImpl)) {
                if (isUserTask(activityImpl)) {
                    rtnList.add(activityImpl);
                } else if (isMultiInstanceBody(activityImpl)) {
                    for (ActivityImpl instanceBodyActivity : activityImpl.getActivities()) {
                        if (isUserTask(instanceBodyActivity)) {
                            // 加完一个直接跳出循环
                            rtnList.add(instanceBodyActivity);
                            break;
                        }
                    }
                } else {
                }
                tempList.add(activityImpl);
                iterateBackActivity(activityImpl, rtnList, tempList);
            }
        }

        return rtnList;
    }
    /**
     * 根据任务id和节点id获取活动节点
     * @param taskId
     * @param activityId 节点的taskDefinitionKey
     *                   如果为null或""，则查询当前活动节点
     *                   如果为“end”，则查询结束节点
     * @return
     */
    public static ActivityImpl findActivityImpl(String taskId, String activityId) {
        // 获取流程定义
        ProcessDefinitionEntity pd = findProcessDefinitionEntityByTaskId(taskId);

        // 获取当前活动节点id
        if (StringUtils.isEmpty(activityId)) {
            activityId = TaskServiceUtil.getTaskByTaskId(taskId).getTaskDefinitionKey();
        }

        // 根据流程定义，获取该流程实例的结束节点
        if ("end".equals(activityId)) {
            for (ActivityImpl activity : pd.getActivities()) {
                List<PvmTransition> outgoingTransitions = activity.getOutgoingTransitions();
                if (outgoingTransitions.isEmpty()) {
                    return activity;
                }
            }
        }

        ActivityImpl activityImpl = pd.findActivity(activityId);
        if (activityImpl.isMultiInstance() && activityImpl instanceof ActivityImpl) {
            return (ActivityImpl) activityImpl.getFlowScope();
        }
        return activityImpl;
    }

    // 获取活动节点的属性
    public static Object getActivityProperty(ActivityImpl activity, String propertyName) {
        return activity.getProperty(propertyName);
    }

    // 判断节点是否为指定类型
    public static boolean isSpecificType(ActivityImpl activity, String type) {
        return type.equals(getActivityProperty(activity, "type"));
    }

    // 判断节点是否为userTask
    public static boolean isUserTask(ActivityImpl activity) {
        return isSpecificType(activity, "userTask");
    }

    // 判断节点是否为multiInstanceBody
    public static boolean isMultiInstanceBody(ActivityImpl activity) {
        return isSpecificType(activity, "multiInstanceBody");
    }

    // 判断节点是否为排他网关
    public static boolean isExclusiveGateway(ActivityImpl activity) {
        return isSpecificType(activity, "exclusiveGateway");
    }
    // --------------------------------查询可驳回的节点end------------------------------------
}
