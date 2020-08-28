package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSettingHis {
    /**
     * 历史表主键ID
     */
    private String id;
    /**
     * 任务设置表ID
     */
    private String taskSettingId;
    /**
     * 流程定义
     */
    private String processDefinitionId;
    /**
     * 流程定义KEY
     */
    private String processDefinitionKey;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 节点KEY
     */
    private String taskDefinitionKey;
    /**
     * 节点类型
     */
    private String taskSettingType;
    /**
     * 接口ID
     */
    private String apiId;
    /**
     * 审批人
     */
    private String approvers;
    /**
     * 审批选项枚举
     */
    private String approveOptions;
    /**
     * 操作选项枚举
     */
    private String operActions;
    /**
     * appSecret
     */
    private String appSecret;
}
