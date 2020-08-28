
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.model.TaskAPISearch;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.TaskExt;

/**
 * 任务服务类
 */
public interface TaskAPIService {
    /**
     * 查询所有任务
     *
     * @param taskSearch 查询条件
     * @return 分页列表
     */
    PageResult<TaskExt> findAllTasks(TaskAPISearch taskSearch) throws UnitedException;

    /**
     * 查询未完成待办
     *
     * @param taskSearch 查询条件
     * @return 分页列表
     */
    PageResult<TaskExt> findTodoTasks(TaskAPISearch taskSearch) throws UnitedException;

    TaskExt findSingleTask(String taskId) throws UnitedException;

}
