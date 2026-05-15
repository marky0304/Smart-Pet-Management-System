package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社区点赞记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_likes")
public class CommunityLike {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 点赞目标类型：POST-动态，COMMENT-评论
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID（动态ID或评论ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 点赞目标类型枚举
     */
    public enum TargetType {
        POST("POST", "动态"),
        COMMENT("COMMENT", "评论");

        private final String code;
        private final String desc;

        TargetType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}