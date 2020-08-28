
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.OperHistorySearch;
import me.corningrey.camunda.api.model.PageResult;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.ProcessInstanceExt;
import me.corningrey.camunda.api.model.ProcessOperHistory;
import me.corningrey.camunda.api.model.ProcessSearch;
import org.springframework.stereotype.Service;


@Service
public interface InstanceAPIService {


    PageResult<ProcessOperHistory> findProcessOperHistoryList(OperHistorySearch operHistorySearch) throws UnitedException;

    PageResult<ProcessInstanceExt> findHisInstanceList(ProcessSearch processSearch) throws UnitedException;

}
