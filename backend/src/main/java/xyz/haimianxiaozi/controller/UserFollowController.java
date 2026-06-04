package xyz.haimianxiaozi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.service.UserFollowService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserContext userContext;

    @PostMapping("/{id}/follow")
    public R<String> follow(@PathVariable Long id) {
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId == null) {
            return R.fail(401, "请先登录");
        }
        if (currentUserId.equals(id)) {
            return R.fail("不能关注自己");
        }
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            return R.fail("用户不存在");
        }

        boolean success = userFollowService.follow(currentUserId, id);
        if (!success) {
            return R.fail("已关注");
        }

        User currentUser = userService.getById(currentUserId);
        String nickname = currentUser != null ? currentUser.getNickname() : "用户";
        notificationService.sendNotification(id, "FOLLOW", nickname + " 关注了你", currentUserId);
        return R.ok("关注成功");
    }

    @DeleteMapping("/{id}/follow")
    public R<String> unfollow(@PathVariable Long id) {
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId == null) {
            return R.fail(401, "请先登录");
        }

        boolean success = userFollowService.unfollow(currentUserId, id);
        if (!success) {
            return R.fail("未关注");
        }
        return R.ok("取消关注成功");
    }

    @GetMapping("/{id}/follow-status")
    public R<Map<String, Object>> followStatus(@PathVariable Long id) {
        Long currentUserId = userContext.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("following", currentUserId != null && userFollowService.isFollowing(currentUserId, id));
        result.put("followingCount", userFollowService.countFollowing(id));
        result.put("followerCount", userFollowService.countFollowers(id));
        return R.ok(result);
    }
}
