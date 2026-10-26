package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.TaskRequestDTO;
import com.pfe.mamacareplus.dto.TaskResponseDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.Task;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final FemmeRepository femmeRepository;
    private final CurrentUserService currentUserService;

    public TaskService(
            TaskRepository taskRepository,
            FemmeRepository femmeRepository,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.femmeRepository = femmeRepository;
        this.currentUserService = currentUserService;
    }

    public List<TaskResponseDTO> getAllTasks() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return taskRepository.findByFemmeFemmeIdOrderByDueDateAsc(currentFemmeId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponseDTO getTaskById(Integer taskId) {
        Task task = findTaskById(taskId);
        currentUserService.checkFemmeAccess(task.getFemme().getFemmeId());

        return mapToResponse(task);
    }

    public List<TaskResponseDTO> getTasksByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        return taskRepository.findByFemmeFemmeIdOrderByDueDateAsc(femmeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TaskResponseDTO> getTasksByFemmeIdAndCompletion(Integer femmeId, Boolean isCompleted) {
        currentUserService.checkFemmeAccess(femmeId);

        return taskRepository.findByFemmeFemmeIdAndIsCompletedOrderByDueDateAsc(femmeId, isCompleted)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponseDTO createTask(TaskRequestDTO request) {
        validateCreateRequest(request);
        currentUserService.checkFemmeAccess(request.getFemmeId());

        Femme femme = femmeRepository.findById(request.getFemmeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));

        Task task = new Task();
        task.setFemme(femme);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setIsCompleted(request.getIsCompleted() != null ? request.getIsCompleted() : false);

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponseDTO updateTask(Integer taskId, TaskRequestDTO request) {
        Task task = findTaskById(taskId);
        currentUserService.checkFemmeAccess(task.getFemme().getFemmeId());

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getIsCompleted() != null) {
            task.setIsCompleted(request.getIsCompleted());
        }

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponseDTO markTaskCompleted(Integer taskId) {
        Task task = findTaskById(taskId);
        currentUserService.checkFemmeAccess(task.getFemme().getFemmeId());

        task.setIsCompleted(true);
        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponseDTO markTaskNotCompleted(Integer taskId) {
        Task task = findTaskById(taskId);
        currentUserService.checkFemmeAccess(task.getFemme().getFemmeId());

        task.setIsCompleted(false);
        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Integer taskId) {
        Task task = findTaskById(taskId);
        currentUserService.checkFemmeAccess(task.getFemme().getFemmeId());

        taskRepository.delete(task);
    }

    private Task findTaskById(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private void validateCreateRequest(TaskRequestDTO request) {
        if (request.getFemmeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Femme ID is required");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
    }

    private TaskResponseDTO mapToResponse(Task task) {
        return new TaskResponseDTO(
                task.getTaskId(),
                task.getFemme().getFemmeId(),
                task.getFemme().getUsername(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getIsCompleted(),
                task.getCreatedAt()
        );
    }
}