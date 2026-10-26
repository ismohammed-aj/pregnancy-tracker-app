package com.pfe.mamacareplus.dto;

public class LoginResponseDTO {

    private String message;
    private UserResponseDTO user;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String message, UserResponseDTO user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() { return message; }
    public UserResponseDTO getUser() { return user; }
}