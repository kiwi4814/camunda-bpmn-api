package me.corningrey.camunda.api.model;

public class CommonConstant {

    /**
     * 流程实例状态：0激活1挂起
     */
    public static final String INST_ACTIVE = "0";
    public static final String INST_SUSPENDED = "1";

    /**
     * 实例完成状态：0完成1未完成
     */
    public static final String INST_FINISHED = "0";
    public static final String INST_UNFINISHED = "1";
    /**
     * 流程打回（流程暂停）
     */
    public static final String SUSPEND_TRUE = "1";

    /**
     * 取消流程打回（流程恢复）
     */
    public static final String SUSPEND_FALSE = "0";
    /**
     * 待办完成状态：0完成1未完成
     */
    public static final String TASK_COMPLETE = "0";
    public static final String TASK_UNCOMPLETE = "1";

    /**
     * 普通待办/自选审批待办
     */
    public static final String COMMON_TASK = "101";
    public static final String OPTIONAL_TASK = "102";

    /**
     * ACTIVE - running process instance
     */
    public static final String INST_STATE_ACTIVE = "ACTIVE";

    /**
     * SUSPENDED - suspended process instances
     */
    public static final String INST_STATE_SUSPENDED = "SUSPENDED";

    /**
     * COMPLETED - completed through normal end event
     */
    public static final String INST_STATE_ = "COMPLETED";

    /**
     * EXTERNALLY_TERMINATED - terminated externally, for instance through REST API
     */
    public static final String INST_STATE_EXTERNALLY_TERMINATED = "EXTERNALLY_TERMINATED";

    /**
     * INTERNALLY_TERMINATED - terminated internally, for instance by terminating boundary event
     */
    public static final String INST_STATE_INTERNALLY_TERMINATED = "INTERNALLY_TERMINATED";
}
