package com.example.multitenantapp.requestDTO;


import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @NotBlank(message = "Description is required")
    private String description;
}
