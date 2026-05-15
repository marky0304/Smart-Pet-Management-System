package com.pet.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityCommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String content;
    private Long parentId;
    private Integer likesCount;
    private Boolean isLiked;
    private List<CommunityCommentVO> replies;
    private LocalDateTime createTime;
}
