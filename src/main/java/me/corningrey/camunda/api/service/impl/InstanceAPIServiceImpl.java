
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.dao.InstanceAPIMapper;
import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.service.InstanceAPIService;
import me.corningrey.camunda.api.service.ProcessInstanceService;
import me.corningrey.camunda.api.util.CamundaUtil;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.CommonConstant;
import me.corningrey.camunda.api.model.ProcessInstanceExt;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import me.corningrey.camunda.api.model.ProcessSearch;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class InstanceAPIServiceImpl implements InstanceAPIService {

    @Resource
    private InstanceAPIMapper instanceAPIMapper;
    @Resource
    private ProcessInstanceService processInstanceService;

    @Override
    public PageResult<ProcessOperHistory> findProcessOperHistoryList(OperHistorySearch operHistorySearch) throws UnitedException {
        if (operHistorySearch == null || StringUtils.isBlank(operHistorySearch.getTenantId())) {
            throw new UnitedException("租户ID为必填！");
        }
        // 处理流程变量相关的参数
        Map<String, Object> variableEquals = CamundaUtil.convertJsonStrToMap(operHistorySearch.getVariables());
        Map<String, Object> variableLike = CamundaUtil.convertJsonStrToMap(operHistorySearch.getVariableLike());
        String variableKeys = CamundaUtil.decode(operHistorySearch.getVariableKeys(), StandardCharsets.UTF_8.name());
        String operTypes = CamundaUtil.decode(operHistorySearch.getOperTypes(), StandardCharsets.UTF_8.name());
        operHistorySearch.setVariableKeys(variableKeys);
        operHistorySearch.setOperTypes(operTypes);
        // 是否分页
        boolean isPage = operHistorySearch.getPageNum() != null && operHistorySearch.getPageLimit() != null;
        if (isPage) {
            PageHelper.startPage(operHistorySearch.getPageNum(), operHistorySearch.getPageLimit());
        }
        // 查询待办列表
        List<ProcessOperHistory> processOperHistoryList = instanceAPIMapper.findProcessOperHistoryList(operHistorySearch, variableEquals, variableLike);
        return (PageResult<ProcessOperHistory>) getPageResultMap(new PageInfo<>(processOperHistoryList));
    }

    @Override
    public PageResult<ProcessInstanceExt> findHisInstanceList(ProcessSearch processSearch) throws UnitedException {
        if (processSearch == null || StringUtils.isBlank(processSearch.getTenancyId())) {
            throw new UnitedException("租户ID为必填！");
        }
        List<String> instanceIdList = null;
        String assignee = CamundaUtil.decode(processSearch.getAssignee(), StandardCharsets.UTF_8.name());
        // 判断该用户参与审批/发起的流程，如果为空则直接返回
        if (StringUtils.isNotBlank(assignee)) {
            instanceIdList = processInstanceService.findInstanceIdsByAssignee(processSearch.getTenancyId(), assignee);
            if (instanceIdList == null || instanceIdList.isEmpty()) {
                return null;
            }
        }
        // 处理逗号拼接的参数
        processSearch.setInstanceIds(CamundaUtil.decode(processSearch.getInstanceIds(), StandardCharsets.UTF_8.name()));
        processSearch.setState(CamundaUtil.decode(processSearch.getState(), StandardCharsets.UTF_8.name()));
        processSearch.setVariableKeys(CamundaUtil.decode(processSearch.getVariableKeys(), StandardCharsets.UTF_8.name()));
        processSearch.setDeleteReason(CamundaUtil.decode(processSearch.getDeleteReason(), StandardCharsets.UTF_8.name()));
        // 处理流程变量筛选条件，转为Map方便Mybatis处理
        Map<String, Object> variableEquals = CamundaUtil.convertJsonStrToMap(processSearch.getVariablesEquals());
        Map<String, Object> variableLike = CamundaUtil.convertJsonStrToMap(processSearch.getVariablesLike());
        // 兼容旧的参数activeStatus,覆盖查询条件state
        if (StringUtils.isNotBlank(processSearch.getActiveStatus())) {
            String activeStatus = processSearch.getActiveStatus();
            if (StringUtils.equals(CommonConstant.INST_SUSPENDED, activeStatus)) {
                processSearch.setState(CommonConstant.INST_STATE_SUSPENDED);
            } else {
                processSearch.setState(CommonConstant.INST_STATE_ACTIVE);
            }
        }
        // 是否分页
        boolean isPage = processSearch.getPageNum() != null && processSearch.getPageLimit() != null;
        if (isPage) {
            PageHelper.startPage(processSearch.getPageNum(), processSearch.getPageLimit());
        }
        // 查询流程
        List<ProcessInstanceExt> instanceList = instanceAPIMapper.findHisInstanceList(processSearch, variableEquals, variableLike, instanceIdList);
        return (PageResult<ProcessInstanceExt>) getPageResultMap(new PageInfo(instanceList));
    }

    private PageResult<? extends Object> getPageResultMap(PageInfo p) {
        PageResult<?> pageResult = new PageResult<>();
        pageResult.setPageLimit(p.getPageSize());
        pageResult.setPageNum(p.getPageNum());
        pageResult.setTotal(p.getTotal());
        pageResult.setPageCount(p.getPages());
        pageResult.setIsHasNextPage(p.isHasNextPage());
        pageResult.setList(p.getList());
        return pageResult;
    }
}
