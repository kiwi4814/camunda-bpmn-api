
package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.ProcessDefinitionExt;
import me.corningrey.camunda.api.model.ProcessSearch;

import java.util.List;


public interface ProcessDefinitionMapper {

    List<ProcessDefinitionExt> findProcessDefinitionWithType(ProcessSearch processSearch);
}
