package xyz.haimianxiaozi.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String lastMessage;
    private Long unreadCount;
    private LocalDateTime lastMessageAt;
}
