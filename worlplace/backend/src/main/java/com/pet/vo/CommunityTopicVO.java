package com.pet.vo;

import lombok.Data;

@Data
public class CommunityTopicVO {

    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer postsCount;
    private Integer followersCount;
    private Boolean isHot;
    private Integer status;
    private Boolean isFollowed;
}
