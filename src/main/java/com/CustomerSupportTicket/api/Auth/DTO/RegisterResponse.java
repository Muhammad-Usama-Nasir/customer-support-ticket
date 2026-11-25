package com.CustomerSupportTicket.api.Auth.DTO;

public record RegisterResponse(
        Long user_id,
        String user_name,
        String email,
        String role) {
}