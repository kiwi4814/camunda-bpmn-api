package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.TaskSettingHis;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface ProcessSettingMapper {

    /**
     * 查询任务节点配置
     *
     * @param processDefinitionId 流程定义ID
     * @param taskDefinitionKey   节点定义key
     * @return TaskSetting
     */
    TaskSettingHis selectTaskSettingByConditions(@Param("processDefinitionId") String processDefinitionId,
                                                 @Param("taskDefinitionKey") String taskDefinitionKey);

    /**
     * 根据租户ID和类型查找URL
     *
     * @param apiId apiId
     * @return Map<String, String>
     */
    Map<String, String> selectRequestURL(@Param("apiId") String apiId);

    /**
     * 生成processKey
     *
     * @return processKey
     */
    String selectRandomProcessKey();
}
