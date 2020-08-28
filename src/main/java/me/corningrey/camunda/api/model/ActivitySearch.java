package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySearch {

    String processInstanceId;
    String processDefinitionId;
    String actTypes;
}
