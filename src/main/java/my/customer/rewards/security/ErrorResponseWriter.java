package my.customer.rewards.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ErrorResponseWriter {
    private ErrorResponseWriter() { }

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", status == 401 ? "Unauthorized" :
                status == 403 ? "Forbidden" : "Too Many Requests");
        body.put("message", message);
        body.put("path", response.getHeader("X-Request-Path"));
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
