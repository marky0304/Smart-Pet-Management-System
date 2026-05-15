package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.entity.CommunityPost;
import com.pet.entity.CommunityTopic;
import com.pet.service.CommunityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CommunityAgent implements Agent {

    private final CommunityService communityService;

    public CommunityAgent(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public DomainType getDomain() {
        return DomainType.COMMUNITY;
    }

    @Override
    public String getName() {
        return "社区互动Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        return PromptTemplates.buildPrompt(PromptTemplates.COMMUNITY_ROLE, operation.name());
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        StringBuilder sb = new StringBuilder();
        Long userId = ctx.getUserId();

        sb.append("【热门话题】\n");
        try {
            List<CommunityTopic> hotTopics = communityService.getHotTopics(10);
            if (hotTopics.isEmpty()) {
                sb.append("  暂无热门话题\n");
            } else {
                for (CommunityTopic t : hotTopics) {
                    sb.append("#").append(t.getName());
                    if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                        sb.append(" — ").append(t.getDescription());
                    }
                    sb.append("  (").append(t.getPostsCount() != null ? t.getPostsCount() : 0).append("条动态");
                    if (t.getFollowersCount() != null && t.getFollowersCount() > 0) {
                        sb.append(",").append(t.getFollowersCount()).append("人关注");
                    }
                    sb.append(")\n");
                }
            }
        } catch (Exception e) {
            sb.append("  话题数据加载失败\n");
        }

        sb.append("\n【热门动态】\n");
        try {
            List<CommunityPost> hotPosts = communityService.getHotPosts(5, userId);
            if (hotPosts.isEmpty()) {
                sb.append("  暂无热门动态\n");
            } else {
                for (CommunityPost p : hotPosts) {
                    sb.append("- ");
                    String content = p.getContent();
                    if (content != null) {
                        sb.append(content.length() > 80 ? content.substring(0, 80) + "..." : content);
                    }
                    if (p.getTopicTags() != null && !p.getTopicTags().isEmpty()) {
                        sb.append("  #").append(p.getTopicTags().replace(",", " #"));
                    }
                    sb.append("  [").append(p.getLikesCount() != null ? p.getLikesCount() : 0).append("赞");
                    sb.append(" ").append(p.getCommentsCount() != null ? p.getCommentsCount() : 0).append("评]");
                    if (p.getLocation() != null && !p.getLocation().isEmpty()) {
                        sb.append(" @").append(p.getLocation());
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  动态数据加载失败\n");
        }

        sb.append("\n【我的近期动态】\n");
        try {
            List<CommunityPost> myPosts = communityService.getPostsByUser(1, 5, userId, userId).getRecords();
            if (myPosts.isEmpty()) {
                sb.append("  你还没有发布过动态，快去分享毛孩子的日常吧！\n");
            } else {
                for (CommunityPost p : myPosts) {
                    String content = p.getContent();
                    sb.append("- ");
                    if (content != null) {
                        sb.append(content.length() > 60 ? content.substring(0, 60) + "..." : content);
                    }
                    sb.append("  [").append(p.getLikesCount() != null ? p.getLikesCount() : 0).append("赞")
                      .append(" ").append(p.getCommentsCount() != null ? p.getCommentsCount() : 0).append("评]");
                    if (p.getTopicTags() != null && !p.getTopicTags().isEmpty()) {
                        sb.append("  #").append(p.getTopicTags().replace(",", " #"));
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  个人动态加载失败\n");
        }

        sb.append("\n【推荐宠友】\n");
        try {
            List<Object> users = communityService.getRecommendUsers(userId, 5);
            if (users == null || users.isEmpty()) {
                sb.append("  暂无推荐用户\n");
            } else {
                for (Object u : users) {
                    if (u instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) u;
                        Object username = map.get("username");
                        sb.append("- ").append(username != null ? username : "宠友");
                        Object petCount = map.get("petCount");
                        if (petCount != null) {
                            sb.append("  (").append(petCount).append("只毛孩子)");
                        }
                        sb.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            sb.append("  推荐用户加载失败\n");
        }

        return sb.toString();
    }
}
