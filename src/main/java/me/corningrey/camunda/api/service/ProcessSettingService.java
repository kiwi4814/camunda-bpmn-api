package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.TaskSettingHis;
import me.corningrey.camunda.api.model.UnitedException;

import java.util.Map;

public interface ProcessSettingService {
    /**
     * 根据processDefinitionId和taskKey查找设置信息
     *
     * @param taskDefinitionKey 节点定义key
     * @return TaskSetting
     */
    TaskSettingHis selectTaskSettingByConditions(String processDefinitionId, String taskDefinitionKey) throws UnitedException;

    /**
     * 根据租户ID和类型查找URL
     *
     * @param apiId          类型：用户，组织，角色等
     * @return Map<String, String>
     */
    Map<String, String> selectRequestURL(String apiId) throws UnitedException;

    /**
     * 生成processKey
     * @return processKey
     */
    String selectRandomProcessKey();

}
