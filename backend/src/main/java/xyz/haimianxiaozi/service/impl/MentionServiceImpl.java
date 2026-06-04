package xyz.haimianxiaozi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.MentionService;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.service.UserService;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\p{L}\\p{N}_-]{1,50})");

    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyMentions(String content, Long senderId, String senderName, Long postId) {
        Set<String> usernames = parseUsernames(content);
        if (usernames.isEmpty()) {
            return;
        }

        for (String username : usernames) {
            User user = userService.getByUsername(username);
            if (user == null || user.getId().equals(senderId)) {
                continue;
            }
            notificationService.sendNotification(
                    user.getId(),
                    "MENTION",
                    senderName + " 在帖子或评论中提到了你",
                    postId
            );
        }
    }

    private Set<String> parseUsernames(String content) {
        Set<String> usernames = new LinkedHashSet<>();
        if (content == null || content.isBlank()) {
            return usernames;
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }
}
