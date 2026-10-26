package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.AuthResponseDTO;
import com.pfe.mamacareplus.dto.LoginRequestDTO;
import com.pfe.mamacareplus.dto.RegisterRequestDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.UserRepository;
import com.pfe.mamacareplus.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FemmeRepository femmeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            FemmeRepository femmeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.femmeRepository = femmeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "FEMME");
        user.setIsActive(true);
        user.setIsVerified(false);

        User savedUser = userRepository.save(user);

        Integer femmeId = null;

        if ("FEMME".equalsIgnoreCase(savedUser.getRole())) {
            Femme femme = new Femme();
            femme.setUser(savedUser);
            femme.setUsername(request.getUsername());
            femme.setBirthDate(request.getBirthDate());
            femme.setAvatar(request.getAvatar());

            Femme savedFemme = femmeRepository.save(femme);
            femmeId = savedFemme.getFemmeId();
        }

        String token = jwtService.generateToken(savedUser);

        return new AuthResponseDTO(
                token,
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                femmeId
        );
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("User account is disabled");
        }

        Integer femmeId = femmeRepository.findByUserUserId(user.getUserId())
                .map(Femme::getFemmeId)
                .orElse(null);

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                femmeId
        );
    }
}