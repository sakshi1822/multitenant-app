package com.example.multitenantapp.service;


import com.example.multitenantapp.responseDTO.AuthResponse;
import com.example.multitenantapp.requestDTO.LoginRequest;
import com.example.multitenantapp.requestDTO.RegisterRequest;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.entity.User;
import com.example.multitenantapp.entity.Role;
import com.example.multitenantapp.jwt.JwtUtil;
import com.example.multitenantapp.repository.TenantRepository;
import com.example.multitenantapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, TenantRepository tenantRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        user.setActive(true);

        if (request.getRole() == Role.SUPER_ADMIN.toString()) {
            user.setTenant(null);
        } else {
            if (request.getTenantId() == null) {
                throw new RuntimeException("Tenant ID is required for GROUP_ADMIN and USER roles");
            }
            Tenant tenant = tenantRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            if (!tenant.getActive()) {
                throw new RuntimeException("Tenant is not active");
            }

            user.setTenant(tenant);
        }

        user = userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getTenant() != null) {
            claims.put("tenantId", user.getTenant().getId());
        }
        String token = jwtUtil.generateToken(user, claims);

        return "Registration Successful";
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getTenant() != null) {
            claims.put("tenantId", user.getTenant().getId());
        }

        String token = jwtUtil.generateToken(user, claims);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantName(user.getTenant() != null ? user.getTenant().getTenantName() : null)
                .build();
    }
}
