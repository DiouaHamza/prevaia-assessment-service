package msa.emaia.client.iam.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class UserDto implements UserDetails {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserStatusDto status;
    private RoleDto role;
    private String customerId;
    private String supplierId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of();
        }
        return List.of(role);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != null && !"D".equals(status.getCode());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && "A".equals(status.getCode());
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
