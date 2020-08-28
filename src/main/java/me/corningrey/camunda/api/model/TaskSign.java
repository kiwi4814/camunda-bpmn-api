
package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSign {
    private String plusNodeId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String instanceId;
    private String executionId;
    private String taskDefinitionKey;
    private String taskDefinitionName;
    private String approveOptions;
    private String taskSettingId;
    private String taskSettingType;
    private String taskId;
    private String assignee;
    private String status;
    private String result;
    private String comment;
    private Date startTime;
    private Date endTime;
    private String completionCondition;// 节点的通过条件
    private int finishedCount; // 完成待办数
    private int totalCount; // 总待办数量

}
