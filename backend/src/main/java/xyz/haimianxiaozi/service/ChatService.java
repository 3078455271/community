package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.ChatMessage;
import xyz.haimianxiaozi.vo.ChatMessageVO;
import xyz.haimianxiaozi.vo.ChatSessionVO;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {

    Long sendMessage(Long senderId, Long receiverId, String content);

    Page<ChatMessageVO> getMessages(Long currentUserId, Long targetUserId, int page, int size);

    List<ChatSessionVO> getSessions(Long currentUserId);

    long getUnreadCount(Long currentUserId);

    void markConversationAsRead(Long currentUserId, Long targetUserId);
}
