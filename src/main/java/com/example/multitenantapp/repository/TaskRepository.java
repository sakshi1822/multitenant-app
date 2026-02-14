package com.example.multitenantapp.repository;

import com.example.multitenantapp.entity.Task;
import com.example.multitenantapp.entity.TaskStatus;
import com.example.multitenantapp.entity.Tenant;
import com.example.multitenantapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTenant(Tenant tenant);

    List<Task> findByAssignedTo(User user);

    List<Task> findByStatus(TaskStatus status);



}
