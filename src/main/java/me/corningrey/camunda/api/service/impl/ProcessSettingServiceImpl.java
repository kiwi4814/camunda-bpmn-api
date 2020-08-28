package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.dao.ProcessSettingMapper;
import me.corningrey.camunda.api.model.TaskSettingHis;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.service.ProcessSettingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ProcessSettingServiceImpl implements ProcessSettingService {
    @Resource
    private ProcessSettingMapper processSettingMapper;

    @Override
    public TaskSettingHis selectTaskSettingByConditions(String processDefinitionId, String taskDefinitionKey) throws UnitedException {
        if (StringUtils.isBlank(processDefinitionId)) {
            throw new UnitedException("流程ID不能为空！");
        }
        if (StringUtils.isBlank(taskDefinitionKey)) {
            throw new UnitedException("节点定义key不能为空！");
        }
        return processSettingMapper.selectTaskSettingByConditions(processDefinitionId, taskDefinitionKey);
    }

    @Override
    public Map<String, String> selectRequestURL(String apiId) throws UnitedException {
        if (StringUtils.isBlank(apiId)) {
            throw new UnitedException("apiId不能为空！");
        }
        return processSettingMapper.selectRequestURL(apiId);
    }

    @Override
    public String selectRandomProcessKey() {
        return processSettingMapper.selectRandomProcessKey();
    }
}
