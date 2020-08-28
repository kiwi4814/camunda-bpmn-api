
package me.corningrey.camunda.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.dao.TaskAgentMapper;
import me.corningrey.camunda.api.model.TaskAgent;
import me.corningrey.camunda.api.service.ProcessOperHistoryService;
import me.corningrey.camunda.api.service.TaskAgentService;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.TaskSearch;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TaskAgentServiceImpl implements TaskAgentService {
    @Resource
    TaskAgentMapper taskAgentMapper;
    @Resource
    TaskService taskService;
    @Resource
    private ProcessOperHistoryService processOperHistoryService;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void setAgent(String taskId, String operUser, String principal, String agent, String reason) throws UnitedException {
        // 校验参数
        if (StringUtils.isBlank(taskId)) {
            throw new UnitedException("taskId not found！");
        }
        if (StringUtils.isBlank(principal)) {
            throw new UnitedException("principal not found！");
        }
        if (StringUtils.isBlank(agent)) {
            throw new UnitedException("agent not found！");
        }

        //判断任务是否已存在代理记录
        TaskAgent ta = getAgentTask(taskId);
        if (ta != null) {
            throw new UnitedException("task already exist agent");
        }
        taskAgentMapper.insert(TaskAgent.builder().taskId(taskId).agent(agent).principal(principal).isCompleted("n").build());

        // 插入操作日志（comment "给用户:" + principal + "设置代理人:" + agent）
        JSONObject jsonObject = new JSONObject();
        // 操作人
        jsonObject.put("operUser", operUser);
        // 当事人
        jsonObject.put("principal", principal);
        // 审批意见
        jsonObject.put("agent", agent);
        String operComment = jsonObject.toString();
        processOperHistoryService.insertTaskHistory(taskId, ProcessOperEnum.AGENT.getValue(), operUser, operComment, reason);
    }

    @Override
    public List<TaskAgent> getMyAgentTasks(String agent) throws UnitedException {
        // return taskAgentMapper.findByAgent(agent);
        TaskSearch taskSearch = new TaskSearch();
        taskSearch.setAgent(agent);
        return taskAgentMapper.findAgentInfo(taskSearch);
    }

    @Override
    public TaskAgent getAgentTask(String taskId) {
        // return taskAgentMapper.findAgentByTaskId(taskId);
        TaskSearch taskSearch = new TaskSearch();
        taskSearch.setTaskId(taskId);
        List<TaskAgent> list = taskAgentMapper.findAgentInfo(taskSearch);
        if (list.isEmpty()) {
            return null;
        }
        // 正常情况下，要么查出一条，要么结果为空
        return list.get(0);
    }

    @Override
    public List<TaskAgent> findAgentInfo(TaskSearch taskSearch) {
        return taskAgentMapper.findAgentInfo(taskSearch);
    }

    @Override
    public void finishTask(String taskId, String assignee) {
        taskAgentMapper.finishTask(taskId, assignee);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void setAssignee(String taskId, String operUser, String inheritor, String reason) throws UnitedException {
        // 校验参数
        if (StringUtils.isBlank(taskId)) {
            throw new UnitedException("taskId为空！");
        }
        if (StringUtils.isBlank(operUser)) {
            throw new UnitedException("操作人operUser为空！");
        }
        if (StringUtils.isBlank(inheritor)) {
            throw new UnitedException("转办处理人inheritor为空！");
        }

        taskService.setAssignee(taskId, inheritor);

        // 插入操作日志（"任务转交给:" + inheritor）
        JSONObject jsonObject = new JSONObject();
        // 操作人（这里就是审批人）
        jsonObject.put("operUser", operUser);
        // 新的处理人
        jsonObject.put("inheritor", inheritor);
        String operComment = jsonObject.toString();
        processOperHistoryService.insertTaskHistory(taskId, ProcessOperEnum.DELIVER.getValue(), operUser, operComment, reason);
    }

}
