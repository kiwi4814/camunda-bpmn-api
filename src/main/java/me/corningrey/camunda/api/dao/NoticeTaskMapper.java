package me.corningrey.camunda.api.dao;

import me.corningrey.camunda.api.model.NoticeTask;
import org.apache.ibatis.annotations.Param;

public interface NoticeTaskMapper {

    /**
     * 新增传阅人
     *
     * @param noticeTask 传阅
     */
    int insert(NoticeTask noticeTask);

    int updateByPrimaryKey(@Param("id") String id,@Param("assignee")String assignee,@Param("reason") String reason);

    NoticeTask selectByPrimaryKey(String id);

    void deleteByPrimaryKey(String id);

}
