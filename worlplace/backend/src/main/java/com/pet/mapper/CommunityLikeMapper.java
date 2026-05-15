package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.CommunityLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommunityLikeMapper extends BaseMapper<CommunityLike> {

    @Select("SELECT COUNT(*) > 0 FROM community_likes " +
            "WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    boolean checkUserLiked(@Param("userId") Long userId,
                          @Param("targetType") String targetType,
                          @Param("targetId") Long targetId);

    @Delete("DELETE FROM community_likes " +
            "WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int deleteLike(@Param("userId") Long userId,
                   @Param("targetType") String targetType,
                   @Param("targetId") Long targetId);
}