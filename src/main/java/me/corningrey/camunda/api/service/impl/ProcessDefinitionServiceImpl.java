
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.dao.ProcessDefinitionMapper;
import me.corningrey.camunda.api.service.ProcessDefinitionService;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.ProcessDefinitionExt;
import me.corningrey.camunda.api.model.ProcessSearch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {


    @Resource
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public List<ProcessDefinitionExt> findProcessDefinitionWithType(ProcessSearch processSearch) throws UnitedException {
        if (processSearch == null || StringUtils.isBlank(processSearch.getTenancyId())) {
            throw new UnitedException("请传入租户ID！");
        }
        return processDefinitionMapper.findProcessDefinitionWithType(processSearch);
    }
}
