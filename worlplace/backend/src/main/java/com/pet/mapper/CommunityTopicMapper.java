package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.CommunityTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社区话题Mapper接口
 */
@Mapper
public interface CommunityTopicMapper extends BaseMapper<CommunityTopic> {

    /**
     * 获取热门话题列表
     */
    @Select("SELECT * FROM community_topics " +
            "WHERE status = 1 AND is_hot = 1 " +
            "ORDER BY posts_count DESC " +
            "LIMIT #{limit}")
    List<CommunityTopic> selectHotTopics(@Param("limit") Integer limit);

    /**
     * 根据名称搜索话题
     */
    @Select("SELECT * FROM community_topics " +
            "WHERE status = 1 AND name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY posts_count DESC")
    List<CommunityTopic> searchTopicsByName(@Param("keyword") String keyword);

    /**
     * 增加话题动态数
     */
    @Update("UPDATE community_topics SET posts_count = posts_count + 1 WHERE id = #{topicId}")
    int increasePostsCount(@Param("topicId") Long topicId);

    /**
     * 减少话题动态数
     */
    @Update("UPDATE community_topics SET posts_count = posts_count - 1 WHERE id = #{topicId} AND posts_count > 0")
    int decreasePostsCount(@Param("topicId") Long topicId);
}