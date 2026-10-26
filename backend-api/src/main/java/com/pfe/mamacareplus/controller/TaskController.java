package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.TaskRequestDTO;
import com.pfe.mamacareplus.dto.TaskResponseDTO;
import com.pfe.mamacareplus.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{taskId}")
    public TaskResponseDTO getTaskById(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId);
    }

    @GetMapping("/femme/{femmeId}")
    public List<TaskResponseDTO> getTasksByFemmeId(@PathVariable Integer femmeId) {
        return taskService.getTasksByFemmeId(femmeId);
    }

    @GetMapping("/femme/{femmeId}/completed/{isCompleted}")
    public List<TaskResponseDTO> getTasksByFemmeIdAndCompletion(
            @PathVariable Integer femmeId,
            @PathVariable Boolean isCompleted
    ) {
        return taskService.getTasksByFemmeIdAndCompletion(femmeId, isCompleted);
    }

    @PostMapping
    public TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO request) {
        return taskService.createTask(request);
    }

    @PutMapping("/{taskId}")
    public TaskResponseDTO updateTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody TaskRequestDTO request
    ) {
        return taskService.updateTask(taskId, request);
    }

    @PatchMapping("/{taskId}/complete")
    public TaskResponseDTO markTaskCompleted(@PathVariable Integer taskId) {
        return taskService.markTaskCompleted(taskId);
    }

    @PatchMapping("/{taskId}/uncomplete")
    public TaskResponseDTO markTaskNotCompleted(@PathVariable Integer taskId) {
        return taskService.markTaskNotCompleted(taskId);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}