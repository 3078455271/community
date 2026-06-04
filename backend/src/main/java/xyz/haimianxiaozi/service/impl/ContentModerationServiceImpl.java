package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.SensitiveWord;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.SensitiveWordMapper;
import xyz.haimianxiaozi.service.ContentModerationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SensitiveWordMapper sensitiveWordMapper;

    @Override
    public String findSensitiveWord(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String lowerContent = content.toLowerCase();
        return sensitiveWordMapper.selectList(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getEnabled, 1))
                .stream()
                .map(SensitiveWord::getWord)
                .filter(word -> word != null && !word.isBlank())
                .filter(word -> lowerContent.contains(word.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isMuted(User user) {
        return user != null && user.getMutedUntil() != null && user.getMutedUntil().isAfter(LocalDateTime.now());
    }

    @Override
    public String muteMessage(User user) {
        if (!isMuted(user)) {
            return "账号未禁言";
        }
        return "账号已被禁言至 " + user.getMutedUntil().format(FORMATTER);
    }
}
