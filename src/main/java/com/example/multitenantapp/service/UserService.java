package com.example.multitenantapp.service;


import com.example.multitenantapp.requestDTO.UserRequest;
import com.example.multitenantapp.responseDTO.UserResponse;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.entity.User;
import com.example.multitenantapp.entity.Role;
import com.example.multitenantapp.repository.TenantRepository;
import com.example.multitenantapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> users;

        if (currentUser.getRole() == Role.SUPER_ADMIN) {

            users = userRepository.findAll();
        } else if (currentUser.getRole() == Role.GROUP_ADMIN) {

            users = userRepository.findByTenant(currentUser.getTenant());
        } else {

            throw new RuntimeException("Access denied");
        }

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return convertToUserResponse(user);
        }

        if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() != null &&
                    user.getTenant() != null &&
                    currentUser.getTenant().getId().equals(user.getTenant().getId())) {
                return convertToUserResponse(user);
            }
            throw new RuntimeException("Access denied to this user");
        }

        if (currentUser.getId().equals(id)) {
            return convertToUserResponse(user);
        }

        throw new RuntimeException("Access denied");
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {

        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();


        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        return convertToUserResponse(currentUser);
    }


    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByTenant(Long tenantId) {

        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));


        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            List<User> users = userRepository.findByTenantId(tenantId);
            return users.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
        }

        if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() != null && currentUser.getTenant().getId().equals(tenantId)) {
                List<User> users = userRepository.findByTenantId(tenantId);
                return users.stream()
                        .map(this::convertToUserResponse)
                        .collect(Collectors.toList());
            }
            throw new RuntimeException("Access denied to this tenant's users");
        }

        throw new RuntimeException("Access denied");
    }


    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantName(user.getTenant() != null ? user.getTenant().getTenantName() : null)
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }


}