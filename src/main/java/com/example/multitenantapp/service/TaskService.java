package com.example.multitenantapp.service;

import com.example.multitenantapp.entity.TaskStatus;
import com.example.multitenantapp.requestDTO.TaskRequest;
import com.example.multitenantapp.responseDTO.TaskResponse;
import com.example.multitenantapp.entity.Task;
import com.example.multitenantapp.entity.User;
import com.example.multitenantapp.entity.Role;
import com.example.multitenantapp.repository.TaskRepository;
import com.example.multitenantapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getRole() == Role.USER) {
            throw new RuntimeException("Users cannot create tasks");
        }

        User assignedUser = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() == null ||
                    assignedUser.getTenant() == null ||
                    !currentUser.getTenant().getId().equals(assignedUser.getTenant().getId())) {
                throw new RuntimeException("Cannot assign task to user from different tenant");
            }
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(currentUser);
        task.setLastStatusUpdate(LocalDateTime.now());


        if (assignedUser.getTenant() != null) {
            task.setTenant(assignedUser.getTenant());
        } else {
            throw new RuntimeException("Cannot assign task to user without tenant");
        }

        task = taskRepository.save(task);
        return convertToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<Task> tasks;

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            tasks = taskRepository.findAll();
        } else if (currentUser.getRole() == Role.GROUP_ADMIN) {
            tasks = taskRepository.findByTenant(currentUser.getTenant());
        } else {
            tasks = taskRepository.findByAssignedTo(currentUser);
        }

        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    @Transactional
    public void deleteTask(Long id) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getRole() == Role.USER) {
            throw new RuntimeException("Users cannot delete tasks");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() == null ||
                    task.getTenant() == null ||
                    !currentUser.getTenant().getId().equals(task.getTenant().getId())) {
                throw new RuntimeException("Access denied to this task");
            }
        }

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<Task> tasks = taskRepository.findByAssignedTo(currentUser);
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {

        String username = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(" Current user not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(" Task not found"));

        if (currentUser.getRole() == Role.USER) {
            if (!task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new RuntimeException(" You can update only your own tasks");
            }
        }

        else if (currentUser.getRole() == Role.GROUP_ADMIN) {
            if (currentUser.getTenant() == null ||
                    task.getTenant() == null ||
                    !currentUser.getTenant().getId().equals(task.getTenant().getId())) {

                throw new RuntimeException("You can update only your tenant tasks");
            }
        }
        task.setStatus(status);
        task.setLastStatusUpdate(LocalDateTime.now());
        task.setLastReminderSent(null);

        task = taskRepository.save(task);

        return convertToTaskResponse(task);
    }


    private TaskResponse convertToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .tenantId(task.getTenant() != null ? task.getTenant().getId() : null)
                .tenantName(task.getTenant() != null ? task.getTenant().getTenantName() : null)
                .assignedToId(task.getAssignedTo().getId())
                .assignedToName(task.getAssignedTo().getName())
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
