package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.LoginDTO;
import xyz.haimianxiaozi.dto.RegisterDTO;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.enums.CommonEnums;
import xyz.haimianxiaozi.enums.ErrorCode;
import xyz.haimianxiaozi.enums.RoleEnum;
import xyz.haimianxiaozi.exception.BizException;
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
        if (userService.getByUsername(dto.getUsername()) != null) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(CommonEnums.UserStatus.ENABLED.getCode());
        user.setRole(RoleEnum.USER.getCode());
        userService.save(user);

        return R.ok("注册成功");
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.BAD_CREDENTIALS);
        }

        if (user.getStatus() != null && user.getStatus() == CommonEnums.UserStatus.DISABLED.getCode()) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }

        String role = RoleEnum.fromCode(user.getRole()).getCode();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        Map<String, Object> result = new HashMap<>(2);
        result.put("token", token);

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setRole(role);
        userVO.setCreatedAt(user.getCreatedAt());
        result.put("user", userVO);

        return R.ok(result);
    }
}
