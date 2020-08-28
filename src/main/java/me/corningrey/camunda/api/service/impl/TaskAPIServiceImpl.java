
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.cmd.AllTaskCmd;
import me.corningrey.camunda.api.cmd.TodoTaskCmd;
import me.corningrey.camunda.api.dao.TaskAPIMapper;
import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.model.TaskAPISearch;
import me.corningrey.camunda.api.service.TaskAPIService;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.TaskExt;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TaskAPIServiceImpl implements TaskAPIService {
    @Resource
    private CommandExecutor commandExecutor;
    @Resource
    private TaskAPIMapper taskAPIMapper;
    @Resource
    private TaskService taskService;

    @Override
    public PageResult<TaskExt> findAllTasks(TaskAPISearch taskSearch) throws UnitedException {
        if (taskSearch == null || StringUtils.isBlank(taskSearch.getTenantId())) {
            throw new UnitedException("未指定租户ID！");
        }
        PageInfo<TaskExt> p = commandExecutor.execute(new AllTaskCmd(taskAPIMapper, taskSearch));
        return getPageResultMap(p);
    }

    @Override
    public PageResult<TaskExt> findTodoTasks(TaskAPISearch taskSearch) throws UnitedException {
        if (taskSearch == null || StringUtils.isBlank(taskSearch.getTenantId())) {
            throw new UnitedException("未指定租户ID！");
        }
        PageInfo<TaskExt> p = commandExecutor.execute(new TodoTaskCmd(taskAPIMapper, taskSearch));
        return getPageResultMap(p);
    }

    @Override
    public TaskExt findSingleTask(String taskId) throws UnitedException {
        if (StringUtils.isBlank(taskId)) {
            throw new UnitedException("待办ID不能为空！");
        }
        TaskExt taskExt = new TaskExt();
        
        // 查询此待办的流程变量
        return taskExt;
    }

    private PageResult<TaskExt> getPageResultMap(PageInfo<TaskExt> p) {
        PageResult<TaskExt> pageResult = new PageResult<>();
        pageResult.setPageNum(p.getPageNum());
        pageResult.setPageLimit(p.getPageSize());
        pageResult.setTotal(p.getTotal());
        pageResult.setPageCount(p.getPages());
        pageResult.setIsHasNextPage(p.isHasNextPage());
        pageResult.setList(p.getList());
        return pageResult;
    }
}
