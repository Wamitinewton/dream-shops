package com.newton.dream_shops.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse {
    public String message;
    public Object data;
}
