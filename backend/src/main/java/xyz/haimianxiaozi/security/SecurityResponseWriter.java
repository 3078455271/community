package xyz.haimianxiaozi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import xyz.haimianxiaozi.common.R;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SecurityResponseWriter {

    private SecurityResponseWriter() {
    }

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), R.fail(status, message));
    }
}