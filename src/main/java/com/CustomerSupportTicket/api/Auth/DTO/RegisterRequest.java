package com.CustomerSupportTicket.api.Auth.DTO;

public record RegisterRequest(String full_name,
        String email,
        String password) {

}
