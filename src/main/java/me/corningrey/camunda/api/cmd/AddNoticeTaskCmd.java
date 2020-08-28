package me.corningrey.camunda.api.cmd;

import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.dao.NoticeTaskMapper;
import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.model.NoticeTask;
import me.corningrey.camunda.api.enums.ProcessOperEnum;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import java.util.List;

public class AddNoticeTaskCmd implements Command<Void> {
    /**
     * 传阅的待办
     */
    private String taskId;
    /**
     * 传阅描述
     */
    private String description;
    /**
     * 传阅审批人
     */
    private List<String> userList;
    /**
     * 操作人
     */
    private String operUser;

    private NoticeTaskMapper noticeTaskMapper;

    private TaskAPIMapper taskAPIMapper;

    public AddNoticeTaskCmd(NoticeTaskMapper noticeTaskMapper,TaskAPIMapper taskAPIMapper, String taskId, String description, List<String> userList, String operUser) {
        this.taskId = taskId;
        this.description = description;
        this.userList = userList;
        this.operUser = operUser;
        this.noticeTaskMapper = noticeTaskMapper;
        this.taskAPIMapper = taskAPIMapper;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);
        if (taskEntity == null) {
            throw new RuntimeException("当前任务不存在或者已完成！");
        }
        String processInstanceId = taskEntity.getProcessInstanceId();
        IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
        // 插入历史记录

        userList.forEach(u -> {
            String id = "NOTICE_".concat(idGenerator.getNextId());
            NoticeTask noticeTask = NoticeTask.builder().id(id)
                    .processInstanceId(processInstanceId)
                    .taskId(taskId)
                    .operUser(operUser)
                    .assignee(u)
                    .description(description)
                    .build();
            noticeTaskMapper.insert(noticeTask);

            JSONObject json = new JSONObject();
            if (StringUtils.isBlank(operUser)) {
                operUser = taskEntity.getAssignee();
            }
            json.put("assignee",u);
            json.put("description",description);
            //操作历史
            ProcessOperHistory processOperHistory = ProcessOperHistory.builder()
                    .processInstanceId(noticeTask.getProcessInstanceId())
                    .taskId(noticeTask.getTaskId())
                    .operType(ProcessOperEnum.CIRCULATE.getValue())
                    .operUser(operUser)
                    .operComment(description)
                    .operComment(json.toJSONString()).build();
            taskAPIMapper.insertProcessOperHistory(processOperHistory);
        });
        return null;
    }
}
