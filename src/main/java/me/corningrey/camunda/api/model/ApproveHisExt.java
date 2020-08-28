
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
public class ApproveHisExt {
    private String taskId;
    private String assignee;
    private String owner;
    private String name;
    private String description;
    private Date dueDate;
    private Date followUpDate;
    private int priority;
    private String parentTaskId;
    private String deleteReason;
    private String taskDefinitionKey;
    private String activityInstanceId;
    private String tenantId;
}
