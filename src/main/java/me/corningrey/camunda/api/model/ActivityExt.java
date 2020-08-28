package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityExt {
    /**
     * ID
     */
    String activityId;

    /**
     * 类型
     */
    String activityType;

    /**
     * 名称
     */
    String activityName;

    /**
     * 状态，finished/unfinished
     */
    String status;

    /**
     * 节点对应的线
     */
    String incoming;
}
