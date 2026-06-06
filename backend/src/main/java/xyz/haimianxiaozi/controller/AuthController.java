package xyz.haimianxiaozi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.LoginDTO;
import xyz.haimianxiaozi.dto.RegisterDTO;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.security.AuthCookieManager;
import xyz.haimianxiaozi.security.AuthRateLimiter;
import xyz.haimianxiaozi.security.TokenBlacklistService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.JwtUtil;
import xyz.haimianxiaozi.util.UserContext;
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
    private final UserContext userContext;
    private final AuthCookieManager authCookieManager;
    private final AuthRateLimiter authRateLimiter;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterDTO dto, HttpServletRequest request) {
        if (!authRateLimiter.allow("register", clientIdentity(request, dto.getUsername()))) {
            return R.fail(429, "操作过于频繁，请稍后再试");
        }
        if (userService.getByUsername(dto.getUsername()) != null) {
            return R.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setRole("USER");
        userService.save(user);

        return R.ok("注册成功");
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (!authRateLimiter.allow("login", clientIdentity(request, dto.getUsername()))) {
            return R.fail(429, "操作过于频繁，请稍后再试");
        }

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
        authCookieManager.writeToken(response, token);

        Map<String, Object> result = new HashMap<>();
        result.put("user", toUserVO(user));
        return R.ok(result);
    }

    @GetMapping("/me")
    public R<UserVO> me() {
        User user = userContext.getCurrentUser();
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(toUserVO(user));
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authCookieManager.readToken(request).ifPresent(tokenBlacklistService::blacklist);
        authCookieManager.clearToken(response);
        return R.ok("已退出登录");
    }

    private String clientIdentity(HttpServletRequest request, String username) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
        return ip + ":" + username;
    }

    private UserVO toUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setStatus(user.getStatus());
        userVO.setRole(user.getRole());
        userVO.setMutedUntil(user.getMutedUntil());
        userVO.setCreatedAt(user.getCreatedAt());
        return userVO;
    }
}
