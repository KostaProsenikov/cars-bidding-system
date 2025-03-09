package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class UserEditRequest {

    @Size(max = 20, message = "First name should be 20 characters max.")
    private String firstName;

    @Size(max = 20, message = "Last name should be 20 characters max.")
    private String lastName;

    @Email(message = "Email should be a valid email.")
    private String email;

    @URL(message = "Profile picture should be a valid URL.")
    private String profilePicture;

}
