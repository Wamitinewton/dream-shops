package com.newton.dream_shops.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
}
