
package me.corningrey.camunda.api.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.APIService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CallBackService {
    @Resource
    private DefinedSettings definedSettings;
    @Resource
    private APIService apiService;

    /**
     * execution监听调用的方法
     *
     * @param execution 当前流程绑定的执行实例
     * @param apiId     接口ID
     */
    public void trigger(DelegateExecution execution, String apiId) {
        if (StringUtils.isBlank(apiId)) return;
        Map<String, String> executionMap = CamundaUtil.filterVariablesMap(execution.getVariables(), definedSettings.getFilterVariables());
        executionMap.put("businessKey", execution.getBusinessKey());
        executionMap.put("processInstanceId", execution.getProcessInstanceId());
        executionMap.put("activityId", execution.getCurrentActivityId());
        executionMap.put("activityName", execution.getCurrentActivityName());
        try {
            callApiInterface(apiId, executionMap);
        } catch (Exception e) {
            UnitedLogger.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * task监听调用的方法
     *
     * @param task  当前流程绑定的任务实例
     * @param apiId 接口ID
     */
    public void triggerTask(DelegateTask task, String apiId) {
        if (StringUtils.isBlank(apiId)) return;
        Map<String, String> executionMap = CamundaUtil.filterVariablesMap(task.getVariables(), definedSettings.getFilterVariables());
        executionMap.put("businessKey", task.getExecution().getBusinessKey());
        executionMap.put("processInstanceId", task.getProcessInstanceId());
        executionMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
        executionMap.put("taskDefinitionName", task.getName());
        executionMap.put("taskId", task.getId());
        executionMap.put("assignee", task.getAssignee());
        try {
            callApiInterface(apiId, executionMap);
        } catch (Exception e) {
            UnitedLogger.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 根据apiId调用指定的接口
     *
     * @param apiId        接口ID
     * @param executionMap 接口参数
     */
    private void callApiInterface(String apiId, Map<String, String> executionMap) throws UnitedException {
        APIModel apiModel = apiService.selectById(Long.valueOf(apiId));
        if (apiModel == null) {
            throw new UnitedException("指定的接口不存在！");
        }
        Map<String, String> paramsMap = new HashMap<>();
        JSONArray jsonArray = JSONObject.parseArray(apiModel.getParameter());
        Optional.ofNullable(jsonArray).ifPresent(
                l -> l.forEach(t -> {
                    String key = Optional.ofNullable((JSONObject) t).map(i -> i.getString("key")).orElse("");
                    String value = Optional.ofNullable((JSONObject) t).map(i -> i.getString("default")).orElse("");
                    if (StringUtils.isNotBlank(key)) {
                        paramsMap.put(key, value);
                    }
                })
        );
        if (executionMap != null && !executionMap.isEmpty()) {
            paramsMap.putAll(executionMap);
        }
        String responseText;
        UnitedLogger.error(String.format("[HttpClientUtil]-[%s]URL: %s", apiModel.getRequestMethod(), apiModel.getUrl()));
        UnitedLogger.error(String.format("[HttpClientUtil]-[%s]Params: %s", apiModel.getRequestMethod(), JSON.toJSONString(paramsMap)));
        Instant start = Instant.now();
        switch (apiModel.getRequestMethod()) {
            case ProcessSettingConstant.METHOD_GET:
                responseText = HttpClientUtil.methodGet(apiModel.getUrl(), paramsMap, new HashMap<>());
                break;
            case ProcessSettingConstant.METHOD_POST:
                responseText = HttpClientUtil.methodPost(apiModel.getUrl(), paramsMap, new HashMap<>());
                break;
            default:
                throw new UnitedException("请指定请求方法为GET或POST");
        }
        Instant end = Instant.now();
        UnitedLogger.error(String.format("[HttpClient-Duration Time]: %s ms", Duration.between(start, end).toMillis()));
        CamundaUtil.checkResponseText(responseText, apiModel.getName(), apiModel.getUrl());
    }

}
