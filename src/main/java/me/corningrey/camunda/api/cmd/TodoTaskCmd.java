package me.corningrey.camunda.api.cmd;

import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.model.TaskAPISearch;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.TaskExt;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TodoTaskCmd implements Command<PageInfo<TaskExt>> {
    protected TaskAPISearch taskSearch;

    private TaskAPIMapper taskAPIMapper;


    public TodoTaskCmd(TaskAPIMapper taskAPIMapper, TaskAPISearch taskSearch) {
        this.taskSearch = taskSearch;
        this.taskAPIMapper = taskAPIMapper;
    }


    @Override
    public PageInfo<TaskExt> execute(CommandContext commandContext) {
        // 处理流程变量相关的参数
        Map<String, Object> variableEquals = CamundaUtil.convertJsonStrToMap(taskSearch.getVariableEquals());
        Map<String, Object> variableNotEquals = CamundaUtil.convertJsonStrToMap(taskSearch.getVariableNotEquals());
        Map<String, Object> variableLike = CamundaUtil.convertJsonStrToMap(taskSearch.getVariableLike());
        String variableKeys = CamundaUtil.decode(taskSearch.getVariableKeys(), StandardCharsets.UTF_8.name());
        taskSearch.setVariableKeys(variableKeys);
        // 是否分页
        boolean isPage = taskSearch.getPageNum() != null && taskSearch.getPageLimit() != null;
        if (isPage) {
            PageHelper.startPage(taskSearch.getPageNum(), taskSearch.getPageLimit());
        }
        // 查询待办列表
        List<TaskExt> taskList = taskAPIMapper.findTodoTasks(taskSearch, variableEquals, variableNotEquals, variableLike);
        return new PageInfo<>(taskList);
    }

}
