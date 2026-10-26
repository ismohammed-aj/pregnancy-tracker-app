package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.LoginRequestDTO;
import com.pfe.mamacareplus.dto.LoginResponseDTO;
import com.pfe.mamacareplus.dto.UserRequestDTO;
import com.pfe.mamacareplus.dto.UserResponseDTO;
import com.pfe.mamacareplus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("/register")
    public UserResponseDTO registerUser(@RequestBody UserRequestDTO request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return userService.login(request);
    }

    @PutMapping("/{userId}")
    public UserResponseDTO updateUser(
            @PathVariable Integer userId,
            @RequestBody UserRequestDTO request
    ) {
        return userService.updateUser(userId, request);
    }

    @PatchMapping("/{userId}/activate")
    public UserResponseDTO activateUser(@PathVariable Integer userId) {
        return userService.activateUser(userId);
    }

    @PatchMapping("/{userId}/deactivate")
    public UserResponseDTO deactivateUser(@PathVariable Integer userId) {
        return userService.deactivateUser(userId);
    }

    @PatchMapping("/{userId}/verify")
    public UserResponseDTO verifyUser(@PathVariable Integer userId) {
        return userService.verifyUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}