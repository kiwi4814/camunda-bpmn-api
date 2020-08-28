
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.model.ProcessInstanceExt;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import me.corningrey.camunda.api.model.ProcessSearch;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface InstanceAPIMapper {

    /**
     * 根据条件查询流程实例列表（V1.2）
     *
     * @param oper           查询条件实体
     * @param variableEquals 精确查询流程变量
     * @param variableLike   模糊查询流程变量
     * @return 操作日志列表
     */
    List<ProcessOperHistory> findProcessOperHistoryList(@Param("oper") OperHistorySearch oper,
                                                        @Param("variableEquals") Map<String, Object> variableEquals,
                                                        @Param("variableLike") Map<String, Object> variableLike);

    /**
     * 根据条件查询所有操作日志（V1.2）
     *
     * @param ins            查询条件实体
     * @param variableEquals 精确查询流程变量
     * @param variableLike   模糊查询流程变量
     * @return 流程实例列表
     */
    List<ProcessInstanceExt> findHisInstanceList(@Param("ins") ProcessSearch ins,
                                                 @Param("variableEquals") Map<String, Object> variableEquals,
                                                 @Param("variableLike") Map<String, Object> variableLike,
                                                 @Param("insList") List<String> insList);


}
