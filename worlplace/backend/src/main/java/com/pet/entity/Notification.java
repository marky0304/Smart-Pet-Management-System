package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notifications")
public class Notification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("from_username")
    private String fromUsername;

    @TableField("type")
    private String type;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum NotifyType {
        LIKE("LIKE", "点赞"),
        COMMENT("COMMENT", "评论"),
        FOLLOW("FOLLOW", "关注"),
        SYSTEM("SYSTEM", "系统");

        private final String code;
        private final String label;

        NotifyType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
    }

    public enum TargetType {
        POST("POST", "动态"),
        COMMENT("COMMENT", "评论");

        private final String code;
        private final String label;

        TargetType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
    }
}
