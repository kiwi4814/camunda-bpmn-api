
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.dao.StatisticsMapper;
import me.corningrey.camunda.api.service.StatisticsService;
import me.corningrey.camunda.api.model.UnitedException;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService taskService;

    @Override
    public Map<String, Long> findCommonData(String tenantId, String userId) throws UnitedException {
        if (StringUtils.isBlank(tenantId)) {
            throw new UnitedException("请传入租户ID");
        }
        Map<String, Long> resultMap = new HashMap<>();
        // 运行中的实例数量（激活、未结束）
        HistoricProcessInstanceQuery runningProInsQuery = historyService.createHistoricProcessInstanceQuery().tenantIdIn(tenantId).unfinished().active();
        // 历史实例数量（已结束）
        HistoricProcessInstanceQuery hisProInsQuery = historyService.createHistoricProcessInstanceQuery().tenantIdIn(tenantId).finished();
        // 运行待办数量（激活、未完成）
        TaskQuery taskQuery = taskService.createTaskQuery().tenantIdIn(tenantId).active();
        // 历史待办数量（已完成）
        HistoricTaskInstanceQuery hisTaskQuery = historyService.createHistoricTaskInstanceQuery().tenantIdIn(tenantId).finished();
        long todoAgentTaskCount = 0;
        long finishedAgentTaskCount = 0;
        if (StringUtils.isNotBlank(userId)) {
            runningProInsQuery.startedBy(userId);
            hisProInsQuery.startedBy(userId);
            taskQuery.taskAssignee(userId);
            hisTaskQuery.taskAssignee(userId);
            // 仅当以用户为维度统计时，处理代理的情况
            Map<String, Long> agentTaskCount = statisticsMapper.findAgentTaskCount(tenantId, userId);
            todoAgentTaskCount = agentTaskCount.get("todoAgentTaskCount");
            finishedAgentTaskCount = agentTaskCount.get("finishedAgentTaskCount");

        }
        resultMap.put("runningProcessCount", runningProInsQuery.count());
        resultMap.put("finishedProcessCount", hisProInsQuery.count());
        resultMap.put("unDealedTaskCount", taskQuery.count() + todoAgentTaskCount);
        resultMap.put("completedTaskCount", hisTaskQuery.count() + finishedAgentTaskCount);
        return resultMap;
    }

    @Override
    public Map<String, Long> findCommonDataFromDatabase(String tenantId, String userId) throws UnitedException {
        if (StringUtils.isBlank(tenantId)) {
            throw new UnitedException("请传入租户ID");
        }
        Map<String, Long> resultMap = statisticsMapper.findCommonData(tenantId, userId);
        return resultMap;
    }
}
