
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.TaskAgent;
import me.corningrey.camunda.api.model.TaskSearch;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskAgentMapper {
    int insert(TaskAgent record);

    int finishTask(@Param(value = "taskId")String taskId,@Param(value = "executor")String executor);

    TaskAgent findAgentByTaskId(@Param(value = "taskId")String taskId);

    List<TaskAgent> findByAgent(@Param(value = "agent")String agent);

    List<TaskAgent> findAgentInfo(TaskSearch taskSearch);
}
