package com.CustomerSupportTicket.api.Auth.DTO;

public record LoginResponse(Long user_id,
        String user_name,
        String email,
        String role,
        String access_token,
        String token_type) {
}