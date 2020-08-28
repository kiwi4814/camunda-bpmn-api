package me.corningrey.camunda.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignTask {
    private String taskId;
    private String executionId;
    private String activityId;

    private String signTaskId;
    private String signExecutionId;
    private String signActivityId;

    private String signUser;

    private String variableId;
    private Integer longValue;
    private Long doubleValue;
    private String key;
    private String textValue;
}
