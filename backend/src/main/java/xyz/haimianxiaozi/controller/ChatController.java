package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.ChatMessageDTO;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.ChatService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.ChatMessageVO;
import xyz.haimianxiaozi.vo.ChatSessionVO;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final UserContext userContext;

    @GetMapping("/sessions")
    public R<List<ChatSessionVO>> sessions() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(chatService.getSessions(userId));
    }

    @GetMapping("/messages/{targetUserId}")
    public R<Page<ChatMessageVO>> messages(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(chatService.getMessages(userId, targetUserId, page, size));
    }

    @PostMapping("/messages")
    public R<Long> send(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        if (userId.equals(dto.getReceiverId())) {
            return R.fail("不能给自己发私信");
        }
        User receiver = userService.getById(dto.getReceiverId());
        if (receiver == null) {
            return R.fail("接收人不存在");
        }
        return R.ok(chatService.sendMessage(userId, dto.getReceiverId(), dto.getContent()));
    }

    @PutMapping("/messages/{targetUserId}/read")
    public R<String> markRead(@PathVariable Long targetUserId) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        chatService.markConversationAsRead(userId, targetUserId);
        return R.ok("已读成功");
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(chatService.getUnreadCount(userId));
    }
}
