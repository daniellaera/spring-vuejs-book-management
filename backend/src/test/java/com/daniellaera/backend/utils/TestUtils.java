package com.daniellaera.backend.utils;

import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;

public class TestUtils {

    public static User createUserDto(String firstName, String lastName, String email, String password, Role role) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}