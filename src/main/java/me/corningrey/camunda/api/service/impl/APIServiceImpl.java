
package me.corningrey.camunda.api.service.impl;

import me.corningrey.camunda.api.dao.APIMapper;
import me.corningrey.camunda.api.model.APIModel;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.service.APIService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class APIServiceImpl implements APIService {
    @Resource
    private APIMapper apiMapper;

    @Override
    public APIModel selectById(Long id) throws UnitedException {
        if (id == null) {
            throw new UnitedException("MSG.00002");
        }
        return apiMapper.selectByPrimaryKey(String.valueOf(id));
    }

}
