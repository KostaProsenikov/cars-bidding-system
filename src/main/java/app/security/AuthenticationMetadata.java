package app.security;

import app.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Този клас пази данните на логнатият потребител

@Data
@AllArgsConstructor
public class AuthenticationMetadata implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private UserRole role;
    private boolean isActive;

    //    Този метод се използва oт Spring Security, за да се видят
//    ролите (Roles and Authorities), които логнатият потребител притежава
//    Authority - това означава permission или роля
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

//        hasRole("ADMIN") -> "ROLE_ADMIN
//        hasAuthority("ADMIN") -> ADMIN

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
