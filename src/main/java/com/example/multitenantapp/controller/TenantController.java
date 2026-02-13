package com.example.multitenantapp.controller;


import com.example.multitenantapp.requestDTO.TenantRequest;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.service.TenantService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createTenant(@Valid @RequestBody TenantRequest request) {
        try {
            Tenant tenant = tenantService.createTenant(request);
            return new ResponseEntity<>(tenant, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create tenant", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllTenants() {
        try {
            return ResponseEntity.ok(tenantService.getAllTenants());

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch tenants", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<?> getTenantById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tenantService.getTenantById(id));

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tenant", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantRequest request
    ) {
        try {
            return ResponseEntity.ok(tenantService.updateTenant(id, request));

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update tenant", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteTenant(@PathVariable Long id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete tenant", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
