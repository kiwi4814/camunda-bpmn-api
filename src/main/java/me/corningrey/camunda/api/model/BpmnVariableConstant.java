package me.corningrey.camunda.api.model;


public class BpmnVariableConstant {

    /**
     * 实例名称 instanceRemark
     */
    public static final String INST_REMARK = "instanceRemark";

    /**
     * 全局变量：自选审批人，自动替换下一个节点的审批人
     */
    public static final String OPTIONAL_USERS = "optionalUsers";

    /**
     * 全局变量：替换下一个节点原有的接口参数ids。例如原有审批人为风控部门，可以替换为财务部门
     */
    public static final String OPTIONAL_IDS = "optionalIds";

    /**
     * 全局变量：自选审批人是否为加签，如果为1或者true，则下个节点的审批人为 自选审批人+原有的审批人
     */
    public static final String IS_PLUS_SIGN = "isPlusSign";

    /**
     * 全局变量：如果下一个节点为空，是否自动跳过，如果为1或者true，则流程会跳过此节点，否则流程将无法进行下去
     */
    public static final String IS_PASS_EMPTY_NODE = "isPassEmptyNode";

    /**
     * 全局变量：指定流程实例的最后一位审批人
     */
    public static final String LAST_ASSIGNEE = "lastAssignee";

    /**
     * 全局变量：流程实例当前正在审批的节点key，不包括开始节点和结束节点
     */
    public static final String CURRENT_TASK_DEF_KEY = "currentTaskDefKey";

    /**
     * 后缀，过程变量。当前待办是否是最后一个审批人（taskDefinitionKey_isFinalApprover）
     */
    public static final String SUFFIX_IS_FINAL_APPROVER = "_isFinalApprover";

    /**
     * 后缀，预设置信息。提前设置指定节点的自选审批人，达到指定节点后，此设置就会被清空（taskDefinitionKey_replacedUsers）
     */
    public static final String SUFFIX_REPLACED_USERS = "_replacedUsers";


    /**
     * 后缀，预设置信息。提前设定指定节点的接口参数ids，达到指定节点后，此设置就会被清空（taskDefinitionKey_replacedIds）
     */
    public static final String SUFFIX_REPLACED_IDS = "_replacedIds";

    /**
     * 后缀，预设置信息。提前设定当指定节点未找到审批人时是否要跳过此节点，达到指定节点后，此设置就会被清空（taskDefinitionKey_isPassEmptyNode）
     */
    public static final String SUFFIX_IS_PASS_EMPTY_NODE = "_isPassEmptyNode";

    /**
     * 后缀，预设置信息。设置指定节点的审批人与上一节点审批人有重复时，自动审批重复的待办
     */
    public static final String SUFFIX_IS_AUTO_COMPLETE = "_isAutoComplete";

    /**
     * 后缀，预设置信息。预设置节点审批人是否有额外审批人 taskDefinitionKey_isPlusSign
     */
    public static final String SUFFIX_IS_PLUS_SIGN = "_isPlusSign";

    /**
     * 后缀，历史数据。记录每个节点的审批人列表（如果节点多次审批，只记录最后一次）taskDefinitionKey_approvers
     */
    public static final String SUFFIX_APPROVERS = "_approvers";

    /**
     * 后缀，历史数据。记录每个节点的自选审批人（如果节点多次审批，只记录最后一次）taskDefinitionKey_optionalUsers
     */
    @Deprecated
    public static final String SUFFIX_OPTIONAL_USERS = "_optionalUsers";

    /**
     * 后缀，历史数据。记录每个节点的自选参数（如果节点多次审批，只记录最后一次）taskDefinitionKey_optionalIds
     */
    @Deprecated
    public static final String SUFFIX_OPTIONAL_IDS = "_optionalIds";


    /**
     * 后缀：审批意见变量 taskId_comment
     */
    public static final String SUFFIX_COMMENT = "_comment";

    /**
     * 后缀：审批动作变量 taskId_action
     */
    public static final String SUFFIX_ACTION = "_action";

    /**
     * 后缀：撤回流程标识（用来区分撤回流程中哪些待办是撤回时，未处理的）
     */
    public static final String SUFFIX_CANCEL_REASON = "_cancelProcess";

    /**
     * 后缀：任务对应的节点设置ID taskDefinitionKey_taskSettingId(1.2已废弃)
     */
    @Deprecated
    public static final String SUFFIX_TASKKEY_NODEID = "_taskSettingId";

    /**
     * 后缀：任务对应的节点设置类型 taskDefinitionKey_taskSettingType
     */
    public static final String SUFFIX_TASKKEY_NODETYPE = "_taskSettingType";

    /**
     * 后缀：任务对应的节点审批项 taskDefinitionKey_approveOptions
     */
    public static final String SUFFIX_APPROVEOPTIONS = "_approveOptions";
    /**
     * 后缀：任务对应的节点审批项 taskDefinitionKey_approveOptions
     */
    public static final String SUFFIX_OPERACTIONS = "_operActions";

    /**
     * 后缀：节点的总待办数量 taskDefinitionKey_nrOfInstances
     */
    public static final String SUFFIX_NROFINSTANCES = "_nrOfInstances";

    /**
     * 后缀：节点的已完成待办数量 taskDefinitionKey_nrOfCompletedInstances
     */
    public static final String SUFFIX_NROFCOMPLETEDINSTANCES = "_nrOfCompletedInstances";

    /**
     * 后缀：不同的审批选项对应的投票数变量后缀
     */
    public static final String SUFFIX_COUNT = "_Count";
    public static final String SUFFIX_S_COUNT = "_%s_Count";
}
