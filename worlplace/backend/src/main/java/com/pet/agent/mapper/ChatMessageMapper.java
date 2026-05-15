package com.pet.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.agent.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
