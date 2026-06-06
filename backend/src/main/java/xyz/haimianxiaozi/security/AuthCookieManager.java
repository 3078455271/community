package xyz.haimianxiaozi.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieManager {

    public static final String COOKIE_NAME = "COMMUNITY_AUTH";

    @Value("${security.cookie.secure:false}")
    private boolean secure;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${security.cookie.max-age:86400}")
    private long maxAge;

    public Optional<String> readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public void writeToken(HttpServletResponse response, String token) {
        ResponseCookie cookie = baseCookie(token)
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearToken(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie("")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }
}