package my.customer.rewards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

@Schema(description = "Username and password credentials.")
public class LoginRequest {
    @NotBlank
    @Schema(example = "alice", required = true)
    private String username;
    @NotBlank
    @Schema(example = "password", required = true, format = "password")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
