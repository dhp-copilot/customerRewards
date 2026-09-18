package my.customer.rewards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication result.")
public class LoginResponse {
    @Schema(description = "Bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String token;
    @Schema(example = "alice")
    private final String username;
    @Schema(example = "CUSTOMER")
    private final String role;

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
