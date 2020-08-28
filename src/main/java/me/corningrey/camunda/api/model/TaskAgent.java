
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
public class TaskAgent {
    private String id;
    private String taskId;
    private String agent;
    private String principal;
    private String executor;
    private String isCompleted;
    private Date executeTime;
}
