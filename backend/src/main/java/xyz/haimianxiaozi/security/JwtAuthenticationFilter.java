package xyz.haimianxiaozi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthCookieManager authCookieManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.getUserIdSafely(token);
        if (userId == null) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效");
            return;
        }
        if (tokenBlacklistService.isBlacklisted(token)) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效");
            return;
        }

        User user = userService.getById(userId);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效");
            return;
        }

        LoginUser loginUser = new LoginUser(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                loginUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authCookieManager.readToken(request).orElse(null);
    }
}