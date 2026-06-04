package xyz.haimianxiaozi.service;

import xyz.haimianxiaozi.entity.User;

public interface ContentModerationService {

    String findSensitiveWord(String content);

    boolean isMuted(User user);

    String muteMessage(User user);
}
