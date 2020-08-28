
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.UnitedException;

import java.util.Map;

public interface StatisticsService {

    /**
     * 根据租户id和用户id查询一个系统的流程统计数据
     *
     * @param tenantId
     * @param userId
     * @return
     */
    Map<String, Long> findCommonData(String tenantId, String userId) throws UnitedException;

    Map<String, Long> findCommonDataFromDatabase(String tenantId, String userId) throws UnitedException;

}
