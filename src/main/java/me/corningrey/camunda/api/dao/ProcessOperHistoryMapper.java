
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.ProcessOperHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 操作日志相关接口
 */
public interface ProcessOperHistoryMapper {

    /**
     * 新增操作日志
     *
     * @param processOperHistory 操作日志
     */
    int insert(ProcessOperHistory processOperHistory);

    /**
     * 根据条件查询操作日志列表
     *
     * @param tenantId          租户ID
     * @param processInstanceId 流程实例ID
     * @param taskDefinitionKey 待办taskDefinitionKey
     * @param variables         流程变量
     */
    List<ProcessOperHistory> findByProcessInstanceId(
            @Param("tenantId") String tenantId,
            @Param("processInstanceId") String processInstanceId,
            @Param("taskDefinitionKey") String taskDefinitionKey,
            @Param("variables") Map<String, Object> variables,
            @Param("operTypes") String operTypes);


    /**
     * 根据条件删除操作日志
     *
     * @param params 条件
     */
    void deleteProcessOperHis(@Param("params") Map<String, String> params);

    /**
     * 查询流程打回操作对应的待办id
     *
     * @param processInstanceId 流程实例id
     */
    String queryBackProcessTaskId(String processInstanceId);
}
