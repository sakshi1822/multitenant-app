package com.example.multitenantapp.repository;

import com.example.multitenantapp.entity.Role;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByTenant(Tenant tenant);
    List<User> findByTenantId(Long tenantId);
}
