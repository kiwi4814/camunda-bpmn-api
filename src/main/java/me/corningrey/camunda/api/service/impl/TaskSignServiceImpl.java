
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.cmd.AddSignedTaskCmd;
import me.corningrey.camunda.api.cmd.ManualSignedCmd;
import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.service.TaskSignService;
import me.corningrey.camunda.api.util.CamundaUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TaskSignServiceImpl extends TaskServiceImpl implements TaskSignService {
    @Resource
    private CommandExecutor commandExecutor;
    @Resource
    private TaskAPIMapper taskAPIMapper;

    @Override
    public List<String> runningPlusSign(String taskId, String signUsers) {
        signUsers = CamundaUtil.decode(signUsers, StandardCharsets.UTF_8.name());
        List<String> userList = new ArrayList<>((Arrays.asList(StringUtils.split(signUsers, ","))));
        return commandExecutor.execute(new AddSignedTaskCmd(taskId, userList));
    }

    @Override
    public List<String> manualSignTasks(String taskId, String signUsers, String reason, String operUser) {
        signUsers = CamundaUtil.decode(signUsers, StandardCharsets.UTF_8.name());
        List<String> userList = new ArrayList<>((Arrays.asList(StringUtils.split(signUsers, ","))));
        return commandExecutor.execute(new ManualSignedCmd(taskAPIMapper, taskId, userList, reason, operUser));
    }
}
