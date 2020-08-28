
package me.corningrey.camunda.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantProcess {
    private String id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    /**
     * 实例ID
     */
    private String instanceId;
    /**
     * 流程名称
     */
    private String processName;
    /**
     * bpmn文件路径
     */
    private String filePath;
    private String createUser;
    private String createTime;
    private String updateUser;
    private String updateTime;


}
