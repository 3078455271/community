package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.UpdatePasswordDTO;
import xyz.haimianxiaozi.dto.UpdateUserDTO;
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
    private final PasswordEncoder passwordEncoder;

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

    @PutMapping("/me")
    public R<String> updateCurrentUser(@Valid @RequestBody UpdateUserDTO dto) {
        User user = userContext.getCurrentUser();
        if (user == null) {
            return R.fail(401, "请先登录");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        userService.updateById(user);
        return R.ok("更新成功");
    }

    @PutMapping("/me/password")
    public R<String> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        User user = userContext.getCurrentUser();
        if (user == null) {
            return R.fail(401, "请先登录");
        }

        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return R.fail("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);

        return R.ok("密码修改成功");
    }
}