package com.app.quiz.model;

import lombok.Data;

@Data
public class User {
    private String username;
    private String email;
    private String password;
    private String role; // Add a role field

}
