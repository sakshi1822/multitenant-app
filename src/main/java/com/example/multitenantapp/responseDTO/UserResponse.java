package com.example.multitenantapp.responseDTO;


import com.example.multitenantapp.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long tenantId;
    private String tenantName;
    private Boolean active;
    private LocalDateTime createdAt;
}