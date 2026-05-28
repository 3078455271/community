package xyz.haimianxiaozi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.UserVO;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;

    @GetMapping("/me")
    public R<UserVO> getCurrentUser() {
        User user = userContext.getCurrentUser();
        if (user == null) {
            return R.fail(401, "请先登录");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setCreatedAt(user.getCreatedAt());
        return R.ok(vo);
    }

    @GetMapping("/{id}")
    public R<UserVO> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return R.ok(vo);
    }
}