
package me.corningrey.camunda.api.service;

import java.util.List;
import java.util.Map;

public interface ProcessSettingApiService {

    /**
     * 根据流程设置查找用户列表
     *
     * @param processDefinitionId 流程定义ID
     * @param taskDefinitionKey   节点定义Key
     * @param processVariables    流程变量
     * @return List<String>
     */
    List<String> getUserListByTaskSetting(String processDefinitionId, String taskDefinitionKey, Map<String, String> processVariables) throws Exception;
}
