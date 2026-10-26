package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.LoginRequestDTO;
import com.pfe.mamacareplus.dto.LoginResponseDTO;
import com.pfe.mamacareplus.dto.UserRequestDTO;
import com.pfe.mamacareplus.dto.UserResponseDTO;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponseDTO getUserById(Integer userId) {
        User user = findUserById(userId);
        return mapToResponse(user);
    }

    public UserResponseDTO registerUser(UserRequestDTO request) {
        validateUserRequest(request, true);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null || request.getRole().isBlank() ? "FEMME" : request.getRole());
        user.setIsActive(true);
        user.setIsVerified(false);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        return new LoginResponseDTO("Login successful", mapToResponse(user));
    }

    public UserResponseDTO updateUser(Integer userId, UserRequestDTO request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole());
        }

        return mapToResponse(userRepository.save(user));
    }

    public UserResponseDTO activateUser(Integer userId) {
        User user = findUserById(userId);
        user.setIsActive(true);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponseDTO deactivateUser(Integer userId) {
        User user = findUserById(userId);
        user.setIsActive(false);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponseDTO verifyUser(Integer userId) {
        User user = findUserById(userId);
        user.setIsVerified(true);
        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(Integer userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void validateUserRequest(UserRequestDTO request, boolean requirePassword) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (requirePassword && (request.getPassword() == null || request.getPassword().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private UserResponseDTO mapToResponse(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getIsVerified()
        );
    }
}