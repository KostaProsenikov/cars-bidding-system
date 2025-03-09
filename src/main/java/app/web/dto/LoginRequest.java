package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @Size(min = 6, message = "The username must be at least 6 characters!")
    private String username;

    @Size(min = 6, message = "The password must be at least 6 characters!")
    private String password;

}
