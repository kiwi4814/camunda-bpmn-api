package me.corningrey.camunda.api.model;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ProcessInstanceExt", description = "流程实例")
public class ProcessInstanceExt {
    @ApiModelProperty(value = "流程实例ID")
    private String id;
    @ApiModelProperty(value = "流程实例ID")
    private String processInstanceId;
    @ApiModelProperty(value = "流程实例名称")
    private String instanceRemark;
    @ApiModelProperty(value = "业务主键Key")
    private String businessKey;
    @ApiModelProperty(value = "业务名称", hidden = true)
    private String businessName;
    @ApiModelProperty(value = "流程定义ID")
    private String processDefinitionId;
    @ApiModelProperty(value = "流程定义Key")
    private String processDefinitionKey;
    @ApiModelProperty(value = "流程定义名称")
    private String processDefinitionName;
    @ApiModelProperty(value = "流程定义版本（每发布一次版本都会更新）")
    private Integer processDefinitionVersion;
    @ApiModelProperty(value = "流程实例开始时间")
    private Date startTime;
    @ApiModelProperty(value = "流程实例结束时间")
    private Date endTime;
    @ApiModelProperty(value = "流程发起人")
    private String startUserId;
    @ApiModelProperty(value = "流程发起人名称", hidden = true)
    private String startUserName;
    @ApiModelProperty(value = "流程实例状态")
    private String state;
    @ApiModelProperty(value = "流程实例状态名称", hidden = true)
    private String stateName;
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
    @ApiModelProperty(value = "流程实例结束节点ID")
    private String endActivityId;
    @ApiModelProperty(value = "流程实例被删除的原因")
    private String deleteReason;
    @ApiModelProperty(value = "执行实例ID")
    private String executionId;
    @ApiModelProperty(value = "持久化状态", hidden = true)
    private String persistentState;
    @ApiModelProperty(value = "清除时间")
    private Date removalTime;
    @ApiModelProperty(value = "根实例ID")
    private String rootProcessInstanceId;
    @ApiModelProperty(value = "超级实例ID")
    private String superProcessInstanceId;
    @ApiModelProperty(value = "序列计数器")
    private long sequenceCounter;
    @ApiModelProperty(value = "流程实例开始节点ID")
    private String startActivityId;
    @ApiModelProperty(value = "事件类型", hidden = true)
    private String eventType;
    @ApiModelProperty(value = "流程变量列表")
    private List<VariableExt> variableList;
    @ApiModelProperty(value = "与variableKeys参数相对应的流程变量结果Map")
    private JSONObject variableMap;
}
