
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.ProcessOperHistory;

import java.util.List;
import java.util.Map;

/**
 * 操作日志Service类
 */
public interface ProcessOperHistoryService {

    /**
     * 流程打回和恢复操作日志
     *
     * @param taskId     待办id
     * @param reason     原因
     * @param suspendStr 是否需要暂停流程 1（暂停），0（恢复）
     */
    void insertBackHistory(String taskId, String reason, String suspendStr);

    /**
     * 流程撤回操作日志
     *
     * @param processInstanceId 流程实例ID
     * @param operUser          操作人
     * @param reason            撤回原因
     */
    void insertCancelHistory(String processInstanceId, String operUser, String reason);

    /**
     * 流程跳转操作日志
     *
     * @param taskId     待办ID
     * @param activityId 活动节点ID
     */
    void insertTurnHistory(String taskId, String activityId, String operUser, String reason);

    /**
     * 流程跳转连线操作日志
     *
     * @param taskId     待办ID
     * @param sequenceId 活动分支ID
     */
    void insertTurnLineHistory(String taskId, String sequenceId, String operUser, String reason) throws UnitedException;

    /**
     * 待办操作日志
     *
     * @param taskId      待办id
     * @param operType    操作类型
     * @param operUser    操作人
     * @param operComment 操作数据
     */
    void insertTaskHistory(String taskId, String operType, String operUser, String operComment, String reason);

    /**
     * 插入流程操作日志通用接口
     *
     * @param processOperHistory 流程操作日志封装类
     */
    void insertProcessOperHistory(ProcessOperHistory processOperHistory);

    /**
     * 插入操作日志通用接口
     *
     * @param processInstanceId 流程实例ID
     * @param taskId            待办ID
     * @param operType          操作类型
     * @param operUser          操作人
     * @param operComment       操作说明
     */
    void insertProcessOperHistory(String processInstanceId,
                                  String taskId,
                                  String operType,
                                  String operUser,
                                  String operComment,
                                  String reason);

    /**
     * 查询操作日志数据
     *
     * @param operHistorySearch 查询条件
     * @return List<ProcessOperHistory>
     */
    List<ProcessOperHistory> findProcessOperHistory(OperHistorySearch operHistorySearch) throws UnitedException;

    /**
     * 删除流程操作日志
     *
     * @param params 以下组合都可以
     *               1.租户id和流程key
     *               2.流程实例id
     */
    void deleteProcessOperHis(Map<String, String> params);

    /**
     * 查询流程打回操作对应的待办id
     *
     * @param processInstanceId 流程实例id
     * @return 待办ID
     */
    String queryBackProcessTaskId(String processInstanceId);
}
