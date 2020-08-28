
package me.corningrey.camunda.api.service;

import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.ProcessDefinitionExt;
import me.corningrey.camunda.api.model.ProcessSearch;

import java.util.List;


public interface ProcessDefinitionService {

    List<ProcessDefinitionExt> findProcessDefinitionWithType(ProcessSearch processSearch) throws UnitedException;
}
