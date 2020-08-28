package me.corningrey.camunda.api.util;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.*;

/**
 * Created by shucheng on 2019-7-12 下午 13:19
 */
public class RepositoryServiceUtil extends ServiceUtil {

    // --------------------------------流程图信息start------------------------------------
    public static BpmnModelInstance getBpmnModelInstance(String processDefinitionId) {
        return getRepositoryService().getBpmnModelInstance(processDefinitionId);
    }

    /**
     * 根据元素id（流程图上元素id）获取相应的bpmn对象
     *
     * @param processDefinitionId 流程定义id
     * @param elementId           元素id
     * @return
     */
    public static <T extends ModelElementInstance> T queryElementInfoById(String processDefinitionId, String elementId) {
        BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionId);
        return bpmnModelInstance.getModelElementById(elementId);
    }

    /**
     * 根据元素类型（SequenceFlow，StartEvent，EndEvent）获取相应的bpmn对象（可能会有多个）
     *
     * @param processDefinitionId 流程定义id
     * @param referencingClass    元素类型
     * @param <T>
     * @return
     */
    public static <T extends ModelElementInstance> Collection<T> queryElementInfoByType(String processDefinitionId, Class<T> referencingClass) {
        BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionId);
        return bpmnModelInstance.getModelElementsByType(referencingClass);
    }

    /**
     * 根据元素id获取元素的简单信息
     *
     * @param processDefinitionId 流程定义id
     * @param elementId           元素（连线、节点）id
     * @return
     */
    public static Map<String, Object> queryElementSimpleInfoById(String processDefinitionId, String elementId) {
        ModelElementInstance elementInstance = queryElementInfoById(processDefinitionId, elementId);
        return handleModelElementInstance(elementInstance);
    }

    /**
     * 根据元素类型获取元素的简单信息
     * @param processDefinitionId
     * @param referencingClass
     * @param <T>
     * @return
     */
    public static <T extends ModelElementInstance> List<Map<String, Object>> queryElementSimpleInfoByType(String processDefinitionId, Class<T> referencingClass) {
        List<Map<String, Object>> list = new ArrayList<>();
        Collection<T> collection = queryElementInfoByType(processDefinitionId, referencingClass);
        for (T elementInstance : collection) {
            list.add(handleModelElementInstance(elementInstance));
        }
        return list;
    }

    /**
     * 处理单个ModelElementInstance中的信息
     * @param elementInstance
     * @param <T>
     * @return
     */
    public static <T extends ModelElementInstance> Map<String, Object> handleModelElementInstance(T elementInstance) {
        Map<String, Object> resultMap = new HashMap<>();
        if (elementInstance instanceof SequenceFlow) {
            SequenceFlow sequenceFlow = (SequenceFlow) elementInstance;
            resultMap.put("sequenceId", sequenceFlow.getId());
            // 开始节点名称
            resultMap.put("startNodeName", sequenceFlow.getSource().getName());
            // 结束节点名称
            resultMap.put("endNodeName", sequenceFlow.getTarget().getName());
        } else if (elementInstance instanceof UserTask) {
            UserTask task = (UserTask) elementInstance;
            // 任务节点的定义key
            resultMap.put("taskDefinitionKey", task.getId());
            // 任务节点的定义名称
            resultMap.put("taskDefinitionName", task.getName());
            // 审批人
            resultMap.put("assignee", task.getCamundaAssignee());
            LoopCharacteristics loopCharacteristics = task.getLoopCharacteristics();
            if (loopCharacteristics != null) {
                if (loopCharacteristics instanceof MultiInstanceLoopCharacteristics) {
                    MultiInstanceLoopCharacteristics multiLoop = (MultiInstanceLoopCharacteristics) loopCharacteristics;
                    // 是否为多人节点
                    resultMap.put("isMulti", true);
                    // 遍历集合表达式
                    resultMap.put("collection", multiLoop.getCamundaCollection());
                    // 完成条件
                    resultMap.put("completionCondition", multiLoop.getCompletionCondition().getRawTextContent());
                } else {
                    // 是否为多人节点
                    resultMap.put("isMulti", false);
                }
            }
        }
        return resultMap;
    }

    /**
     * （批量查询）查询多个元素id（流程图上元素id）相应的bpmn对象
     *
     * @param processDefinitionId
     * @param elementIds          多个元素id，以逗号分隔
     * @return
     */
    public static List<Map<String, Object>> queryBatchElementSimpleInfo(String processDefinitionId, String elementIds) {
        String[] elementIdArr = elementIds.split(",");
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String elementId : elementIdArr) {
            Map<String, Object> simpleInfoMap = queryElementSimpleInfoById(processDefinitionId, elementId);
            resultList.add(simpleInfoMap);
        }
        return resultList;
    }

    /**
     * 根据流程定义id获取通往结束节点的连线id（可能会有多条连线）
     *
     * @param processDefinitionId
     * @return
     */
    public static List<String> queryToEndSequenceIdByDefinitionId(String processDefinitionId) {
        List<String> resultList = new ArrayList<>();
        List<EndEvent> endEventList = (List<EndEvent>) queryElementInfoByType(processDefinitionId, EndEvent.class);
        for (EndEvent e : endEventList) {
            Collection<SequenceFlow> sequenceFlowList = e.getIncoming();
            for (SequenceFlow s : sequenceFlowList) {
                resultList.add(s.getId());
            }
        }
        return resultList;
    }

    /**
     * 根据流程实例id获取通往结束节点的连线id（可能会有多条连线）
     *
     * @param processInstanceId
     * @return
     */
    public static List<String> queryToEndSequenceIdByInstanceId(String processInstanceId) {
        List<String> resultList = new ArrayList<>();
        ProcessInstance processInstance = RuntimeServiceUtil.queryProcessInstanceById(processInstanceId);
        if (processInstance != null) {
            // 获取流程定义id
            String processDefinitionId = processInstance.getProcessDefinitionId();
            resultList = queryToEndSequenceIdByDefinitionId(processDefinitionId);
        }
        return resultList;
    }

    // 获取流程图上所有的连线信息
    public static List<Map<String, String>> queryAllSequences(String processDefinitionId) {
        BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionId);

        List<Map<String, String>> resultList = new ArrayList<>();
        // 获取所有连线
        List<SequenceFlow> sequenceFlows = (List<SequenceFlow>) bpmnModelInstance.getModelElementsByType(SequenceFlow.class);
        // 获取连线的起止节点名称
        for (SequenceFlow s : sequenceFlows) {
            Map<String, String> map = new HashMap<>();
            map.put("sequenceId", s.getId());
            // 起始节点
            if (s.getSource() instanceof StartEvent) {
                map.put("startNodeName", "开始");
            }
            if (s.getSource() instanceof UserTask) {
                UserTask userTask = (UserTask) s.getSource();
                map.put("startNodeName", userTask.getName());
            }
            if (s.getSource() instanceof ExclusiveGateway) {
                map.put("startNodeName", "网关");
            }
            // 结束节点
            if (s.getTarget() instanceof UserTask) {
                UserTask userTask = (UserTask) s.getTarget();
                map.put("endNodeName", userTask.getName());
            }
            if (s.getTarget() instanceof EndEvent) {
                map.put("endNodeName", "结束");
            }
            if (s.getTarget() instanceof ExclusiveGateway) {
                map.put("endNodeName", "网关");
            }
            resultList.add(map);
        }
        return resultList;
    }

    // 获得指定节点上的自定义属性
    public static Object getValueByNodeAttr(String processDefinitionId, String nodeId, String attrName) {
        BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionId);
        Collection<UserTask> userTasks = bpmnModelInstance.getModelElementsByType(UserTask.class);
        for (UserTask userTask : userTasks) {
            if (userTask.getId().equals(nodeId)) {
                CamundaProperties camundaProperties = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaProperties.class).singleResult();
                for (CamundaProperty camundaProperty : camundaProperties.getCamundaProperties()) {
                    if (camundaProperty.getCamundaName().equals(attrName)) {
                        return camundaProperty.getCamundaValue();
                    }
                }
            }
        }
        return null;
    }

    // 判断节点上是否有自定义属性
    public static Object hasNodeAttr(String processDefinitionId, String nodeId, String attrName) {
        Object value = getValueByNodeAttr(processDefinitionId, nodeId, attrName);
        return value != null;
    }
    // --------------------------------流程图信息end------------------------------------
}
