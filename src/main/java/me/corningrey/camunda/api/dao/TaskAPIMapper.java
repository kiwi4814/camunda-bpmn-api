
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.SignTask;
import me.corningrey.camunda.api.model.TaskAPISearch;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import me.corningrey.camunda.api.model.TaskExt;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskAPIMapper {

    List<TaskExt> findTodoTasks(@Param("taskSearch") TaskAPISearch taskSearch,
                                @Param("variableEquals") Map<String, Object> variableEquals,
                                @Param("variableNotEquals") Map<String, Object> variableNotEquals,
                                @Param("variableLike") Map<String, Object> variableLike);


    List<TaskExt> findTasks(@Param("taskSearch") TaskAPISearch taskSearch,
                            @Param("variableEquals") Map<String, Object> variableEquals,
                            @Param("variableNotEquals") Map<String, Object> variableNotEquals,
                            @Param("variableLike") Map<String, Object> variableLike);

    TaskExt findSingleTask(@Param("taskId") String taskId);

    void addSignExecutionEntity(SignTask signTask);

    void addSignActivityEntity(SignTask signTask);

    void addSignTaskEntity(SignTask signTask);

    void addSignTaskHisEntity(SignTask signTask);

    void copySignVariable(SignTask signTask);

    void copySignVariableHis(SignTask signTask);

    void insertProcessOperHistory(ProcessOperHistory processOperHistory);

}
