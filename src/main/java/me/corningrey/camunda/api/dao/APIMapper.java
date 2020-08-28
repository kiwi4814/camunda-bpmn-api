
package me.corningrey.camunda.api.dao;


import me.corningrey.camunda.api.model.APIModel;

public interface APIMapper{

    APIModel selectByPrimaryKey(String id);
}
