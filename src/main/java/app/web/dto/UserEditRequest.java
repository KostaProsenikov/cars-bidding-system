package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEditRequest {

    @Size(min = 3, max = 20, message = "First name should be between 3 and 20 characters.")
    private String firstName;

    @Size(min = 3, max = 20, message = "Last name should be between 3 and 20 characters.")
    private String lastName;

    @Email(message = "Email should be a valid email.")
    private String email;

    @URL(message = "Profile picture should be a valid URL.")
    private String profilePicture;

}
