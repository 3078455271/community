package xyz.haimianxiaozi.service;

public interface MentionService {

    void notifyMentions(String content, Long senderId, String senderName, Long postId);
}
