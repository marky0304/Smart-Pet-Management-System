package com.pet.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommunityPostVO {

    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String content;
    private String images;
    private String topicTags;
    private String location;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer sharesCount;
    private Boolean isTop;
    private Integer status;
    private Boolean isLiked;
    private Boolean isMine;
    private LocalDateTime createTime;
}
