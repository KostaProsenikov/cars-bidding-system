package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Size(min = 6, max = 50, message = "The username must be at least 6 characters and max 50!")
    private String username;

    @Size(min = 6, max = 50, message = "The password must be at least 6 and max 50 characters")
    private String password;

    @Size(min = 6, max = 50, message = "The confirmation password must be at least 6 and max 50 characters")
    private String repeatPassword;

    @Size(min = 3, max = 50, message = "The first name must be at least 3 and max 50 characters")
    private String firstName;

    @Size(min = 3, max = 50, message = "The last name must be at least 3 and max 50 characters")
    private String lastName;

}
