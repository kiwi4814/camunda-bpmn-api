package me.corningrey.camunda.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.cmd.AddNoticeTaskCmd;
import me.corningrey.camunda.api.dao.NoticeTaskMapper;
import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.model.NoticeTask;
import me.corningrey.camunda.api.service.NoticeTaskService;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class NoticeTaskServiceImpl implements NoticeTaskService {
    @Resource
    private CommandExecutor commandExecutor;
    @Resource
    private NoticeTaskMapper noticeTaskMapper;
    @Resource
    private TaskAPIMapper taskAPIMapper;


    @Override
    public void insertNoticeTask(String taskId, String description,String assignees, String operUser) {
        List<String> userList = new ArrayList<>((Arrays.asList(StringUtils.split(assignees, ","))));
        commandExecutor.execute(new AddNoticeTaskCmd(noticeTaskMapper,taskAPIMapper,taskId,description,userList,operUser)) ;
    }

    @Override
    public void approvalNoticeTask(String id,String operUser,String reason) {
        noticeTaskMapper.updateByPrimaryKey(id, "", reason);
        NoticeTask noticeTask = noticeTaskMapper.selectByPrimaryKey(id);


        JSONObject json = new JSONObject();
        if (StringUtils.isBlank(operUser)) {
            operUser = noticeTask.getAssignee();
        }
        json.put("assignee",operUser);
        json.put("reason",reason);

        ProcessOperHistory processOperHistory = ProcessOperHistory.builder()
                .processInstanceId(noticeTask.getProcessInstanceId())
                .taskId(noticeTask.getTaskId())
                .operType(ProcessOperEnum.CIRCULATE.getValue())
                .operUser(operUser)
                .operReason(reason)
                .operComment(json.toJSONString()).build();
        taskAPIMapper.insertProcessOperHistory(processOperHistory);
    }
    @Override
    public void updateAssigneeNoticeTask(String id,String operUser,String assignee) {
        NoticeTask noticeTask = noticeTaskMapper.selectByPrimaryKey(id);
        noticeTaskMapper.updateByPrimaryKey(id, assignee, "");


        JSONObject json = new JSONObject();
        if (StringUtils.isBlank(operUser)) {
            operUser = noticeTask.getAssignee();
        }
        json.put("assignee",noticeTask.getAssignee());
        json.put("newAssignee",assignee);

        ProcessOperHistory processOperHistory = ProcessOperHistory.builder()
                .processInstanceId(noticeTask.getProcessInstanceId())
                .taskId(noticeTask.getTaskId())
                .operType(ProcessOperEnum.CIRCULATE.getValue())
                .operUser(operUser)
                .operComment(json.toJSONString()).build();
        taskAPIMapper.insertProcessOperHistory(processOperHistory);
    }
    @Override
    public void deletAssigneeNoticeTask(String id,String operUser) {
        NoticeTask noticeTask = noticeTaskMapper.selectByPrimaryKey(id);
        noticeTaskMapper.deleteByPrimaryKey(id);

        JSONObject json = new JSONObject();
        if (StringUtils.isBlank(operUser)) {
            operUser = noticeTask.getAssignee();
        }
        json.put("assignee",noticeTask.getAssignee());

        ProcessOperHistory processOperHistory = ProcessOperHistory.builder()
                .processInstanceId(noticeTask.getProcessInstanceId())
                .taskId(noticeTask.getTaskId())
                .operType(ProcessOperEnum.CIRCULATE.getValue())
                .operUser(operUser)
                .operComment(json.toJSONString()).build();
        taskAPIMapper.insertProcessOperHistory(processOperHistory);
    }
}
