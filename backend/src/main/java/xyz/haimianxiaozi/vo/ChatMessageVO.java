package xyz.haimianxiaozi.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String receiverName;
    private String senderAvatar;
    private String receiverAvatar;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
