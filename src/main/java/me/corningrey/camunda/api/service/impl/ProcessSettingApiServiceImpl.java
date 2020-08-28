
package me.corningrey.camunda.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.model.*;
import me.corningrey.camunda.api.service.ProcessSettingApiService;
import me.corningrey.camunda.api.service.ProcessSettingService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessSettingApiServiceImpl implements ProcessSettingApiService {
    @Resource
    private ProcessSettingService processSettingService;

    @Override
    public List<String> getUserListByTaskSetting(String processDefinitionId, String taskDefinitionKey, Map<String, String> paramsMap) throws Exception {
        TaskSettingHis taskSetting = processSettingService.selectTaskSettingByConditions(processDefinitionId, taskDefinitionKey);
        if (taskSetting == null) {
            throw new UnitedException("找不到相关的节点设置");
        }
        // gc_api的ID
        String apiId = taskSetting.getApiId();
        Map<String, String> apiMap = processSettingService.selectRequestURL(apiId);
        // 接口类型（1用户，2团队，3角色，4自定义）
        String approverType = apiMap.get("apiType");
        // 审批人逗号拼接的字符串【针对不同的类型为不同的ID拼接】
        String approvers = taskSetting.getApprovers();
        String optionalIds = paramsMap.get(BpmnVariableConstant.OPTIONAL_IDS);
        paramsMap.put("appSecret", apiMap.get("appSecret"));
        paramsMap.put("approverType", approverType);
        paramsMap.put("ids", StringUtils.isNotBlank(optionalIds) ? optionalIds : approvers);
        String resultJson;
        UnitedLogger.error(String.format("[HttpClientUtil]-[%s]URL: %s", apiMap.get("requestMethod"), apiMap.get("searchUrl")));
        UnitedLogger.error(String.format("[HttpClientUtil]-[%s]Params: %s", apiMap.get("requestMethod"), JSON.toJSONString(paramsMap)));
        Instant start = Instant.now();
        switch (apiMap.get("requestMethod")) {
            case ProcessSettingConstant.METHOD_GET:
                resultJson = HttpClientUtil.methodGet(apiMap.get("searchUrl"), paramsMap, new HashMap<>(1));
                break;
            case ProcessSettingConstant.METHOD_POST:
                resultJson = HttpClientUtil.methodPost(apiMap.get("searchUrl"), paramsMap, new HashMap<>(1));
                break;
            default:
                throw new UnitedException("接口的方法类型必须为GET或者POST的其中一种！");
        }
        Instant end = Instant.now();
        UnitedLogger.error(String.format("[HttpClient-Duration Time]: %s ms", Duration.between(start, end).toMillis()));
        JSONObject dataObject = CamundaUtil.checkResponseText(resultJson, apiMap.get("apiName"), apiMap.get("searchUrl"));
        if (dataObject != null) {
            JSONArray dataListArray = dataObject.getJSONArray("list");
            if (dataListArray == null) {
                throw new UnitedException("接口返回的数据结构不满足要求！data -> list -> {userId: '', userName: ''}");
            }
            List<String> approverList = dataListArray.stream().map(obj -> ((JSONObject) obj).getString("userId")).collect(Collectors.toList());
            return approverList.stream().distinct().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
