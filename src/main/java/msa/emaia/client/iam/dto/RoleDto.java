package msa.emaia.client.iam.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class RoleDto implements GrantedAuthority {
    private String id;
    private String code;
    private String label;

    @Override
    public String getAuthority() {
        return this.code;
    }
}
