
package me.corningrey.camunda.api.dao;


import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface StatisticsMapper {

    Map<String, Long> findAgentTaskCount(@Param("tenantId") String tenantId, @Param("userId") String userId);

    Map<String, Long> findCommonData(@Param("tenantId") String tenantId, @Param("userId") String userId);

}
