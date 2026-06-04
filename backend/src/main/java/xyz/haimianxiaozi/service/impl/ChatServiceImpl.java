package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.ChatMessage;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.ChatMessageMapper;
import xyz.haimianxiaozi.service.ChatService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.ChatMessageVO;
import xyz.haimianxiaozi.vo.ChatSessionVO;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final UserService userService;

    @Override
    public Long sendMessage(Long senderId, Long receiverId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(false);
        save(message);
        return message.getId();
    }

    @Override
    public Page<ChatMessageVO> getMessages(Long currentUserId, Long targetUserId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatMessage::getSenderId, currentUserId).eq(ChatMessage::getReceiverId, targetUserId)
                        .or()
                        .eq(ChatMessage::getSenderId, targetUserId).eq(ChatMessage::getReceiverId, currentUserId))
                .orderByDesc(ChatMessage::getCreatedAt);

        Page<ChatMessage> messagePage = page(new Page<>(page, size), wrapper);
        Page<ChatMessageVO> voPage = new Page<>(page, size, messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(this::toMessageVO)
                .toList());
        return voPage;
    }

    @Override
    public List<ChatSessionVO> getSessions(Long currentUserId) {
        List<ChatMessage> messages = list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, currentUserId)
                .or()
                .eq(ChatMessage::getReceiverId, currentUserId)
                .orderByDesc(ChatMessage::getCreatedAt));
        Map<Long, ChatMessage> latestMap = new LinkedHashMap<>();
        for (ChatMessage message : messages) {
            Long targetUserId = message.getSenderId().equals(currentUserId)
                    ? message.getReceiverId()
                    : message.getSenderId();
            latestMap.putIfAbsent(targetUserId, message);
        }

        return latestMap.entrySet().stream()
                .map(entry -> toSessionVO(currentUserId, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long currentUserId) {
        return count(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, false));
    }

    @Override
    public void markConversationAsRead(Long currentUserId, Long targetUserId) {
        update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, targetUserId)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, false)
                .set(ChatMessage::getIsRead, true));
    }

    private ChatSessionVO toSessionVO(Long currentUserId, Long targetUserId, ChatMessage latestMessage) {
        User targetUser = userService.getById(targetUserId);
        ChatSessionVO vo = new ChatSessionVO();
        vo.setUserId(targetUserId);
        if (targetUser != null) {
            vo.setUsername(targetUser.getUsername());
            vo.setNickname(targetUser.getNickname());
            vo.setAvatar(targetUser.getAvatar());
        }
        vo.setLastMessage(latestMessage.getContent());
        vo.setLastMessageAt(latestMessage.getCreatedAt());
        vo.setUnreadCount(count(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, targetUserId)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, false)));
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessage message) {
        User sender = userService.getById(message.getSenderId());
        User receiver = userService.getById(message.getReceiverId());
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setSenderName(getDisplayName(sender));
        vo.setReceiverName(getDisplayName(receiver));
        vo.setSenderAvatar(sender == null ? null : sender.getAvatar());
        vo.setReceiverAvatar(receiver == null ? null : receiver.getAvatar());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private String getDisplayName(User user) {
        if (user == null) {
            return "用户";
        }
        return user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername();
    }
}
