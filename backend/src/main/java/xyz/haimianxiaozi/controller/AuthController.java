package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.LoginDTO;
import xyz.haimianxiaozi.dto.RegisterDTO;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.JwtUtil;
import xyz.haimianxiaozi.vo.UserVO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userService.getByUsername(dto.getUsername()) != null) {
            return R.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        userService.save(user);

        return R.ok("注册成功");
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            return R.fail("用户名或密码错误");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return R.fail("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return R.fail("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setCreatedAt(user.getCreatedAt());
        result.put("user", userVO);

        return R.ok(result);
    }
}