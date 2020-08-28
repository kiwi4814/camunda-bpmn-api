
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.ActivityExt;
import me.corningrey.camunda.api.model.ActivitySearch;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ProcessInstanceMapper {

    List<ActivityExt> findHistoricActivityList(ActivitySearch activitySearch);

    Map<String, String> findProcessInstanceStatus(@Param("instanceId") String instanceId, @Param("taskId") String taskId);

    /**
     * 查询指定租户下，审批人关联的流程实例id
     *
     * @param params
     * @return
     */
    List<String> findInstanceIdsByAssignee(Map<String, Object> params);

    String findProcessDefinitionByInstance(@Param("instanceId") String instanceId);

    Map<String, String> findActiveActivity(@Param("instanceId") String instanceId);

    Map<String, String> findInstanceBaseInfoById(@Param("instanceId") String instanceId);
}
