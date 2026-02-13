package com.example.multitenantapp.responseDTO;

import com.example.multitenantapp.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private Role role;
    private Long tenantId;
    private String tenantName;
}
