package com.example.multitenantapp.service;

import com.example.multitenantapp.repository.UserRepository;
import com.example.multitenantapp.requestDTO.TenantRequest;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.entity.User;
import com.example.multitenantapp.entity.Role;
import com.example.multitenantapp.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Tenant createTenant(TenantRequest request) {

        if (tenantRepository.existsByTenantName(request.getTenantName())) {
            throw new RuntimeException("Tenant name already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setTenantName(request.getTenantName());
        tenant.setDescription(request.getDescription());
        tenant.setActive(true);

        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Tenant getTenantById(Long id) {
        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));


        if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() == null || !currentUser.getTenant().getId().equals(id)) {
                throw new RuntimeException("Access denied to this tenant");
            }
        }

        return tenant;
    }


    @Transactional
    public Tenant updateTenant(Long id, TenantRequest request) {

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (!tenant.getTenantName().equals(request.getTenantName()) &&
                tenantRepository.existsByTenantName(request.getTenantName())) {
            throw new RuntimeException("Tenant name already exists");
        }

        tenant.setTenantName(request.getTenantName());
        tenant.setDescription(request.getDescription());

        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(Long id) {

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenantRepository.delete(tenant);
    }
}
