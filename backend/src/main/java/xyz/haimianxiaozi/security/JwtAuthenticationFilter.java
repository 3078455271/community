package xyz.haimianxiaozi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.haimianxiaozi.enums.RoleEnum;
import xyz.haimianxiaozi.util.JwtUtil;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：解析 Authorization 头中的 Bearer token，
 * 校验通过后将身份与角色写入 SecurityContext，交由 Spring Security 做访问控制。
 * 不在此处拒绝请求——未携带/非法 token 时放行，由后续的鉴权规则决定是否拦截。
 *
 * @author haimianxiaozi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtUtil.validateToken(token)) {
            try {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                RoleEnum role = jwtUtil.getRole(token);

                AuthUser principal = new AuthUser(userId, username, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role.authority())));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.debug("解析 token 失败，跳过认证: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
