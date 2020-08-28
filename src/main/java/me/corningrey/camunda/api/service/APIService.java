
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.APIModel;
import me.corningrey.camunda.api.model.UnitedException;

/**
 * @ClassName: TenancyService
 * @Description: 租户管理
 */
public interface APIService {

    public APIModel selectById(Long id) throws UnitedException;

   }
