package com.example.multitenantapp.schedular;

import com.example.multitenantapp.entity.Task;
import com.example.multitenantapp.entity.User;
import com.example.multitenantapp.repository.TaskRepository;
import com.example.multitenantapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import com.example.multitenantapp.entity.TaskStatus;

@Service
@RequiredArgsConstructor
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final MailService mailService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkTasks() {

        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findByStatus(TaskStatus.ASSIGNED);

        for (Task task : tasks) {

            if (task.getLastStatusUpdate() == null) continue;

            long minutes = Duration.between(task.getLastStatusUpdate(), now).toMinutes();

            if (task.getLastReminderSent() != null &&
                    task.getLastStatusUpdate().isAfter(task.getLastReminderSent())) {

                task.setLastReminderSent(null);
                taskRepository.save(task);
                continue;
            }

            // group admin mail
            if (minutes >= 5) {
                User creator = task.getCreatedBy();
                if (creator != null) {
                    mailService.send(
                            creator.getEmail(),
                            "Task Delay Alert",
                            "User " + task.getAssignedTo().getName() +
                                    " has not updated status for task: " + task.getTitle()
                    );
                }

            }

            // user reminder mails
            if (minutes >= 2) {
                long gapMinutes = task.getLastReminderSent() == null ?
                        Long.MAX_VALUE : Duration.between(task.getLastReminderSent(), now).toMinutes();

                if (gapMinutes >= 1) {
                    mailService.send(
                            task.getAssignedTo().getEmail(),
                            "Reminder: Update Task Status",
                            "Please update status for task: " + task.getTitle()
                    );

                    task.setLastReminderSent(now);
                    taskRepository.save(task);
                }
            }
        }
    }
}