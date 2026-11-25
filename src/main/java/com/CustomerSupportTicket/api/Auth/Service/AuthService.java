package com.CustomerSupportTicket.api.Auth.Service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.CustomerSupportTicket.api.Auth.DTO.LoginRequest;
import com.CustomerSupportTicket.api.Auth.DTO.LoginResponse;
import com.CustomerSupportTicket.api.Auth.DTO.ORMGetUser;
import com.CustomerSupportTicket.api.Auth.DTO.RegisterRequest;
import com.CustomerSupportTicket.api.Auth.DTO.RegisterResponse;
import com.CustomerSupportTicket.api.Security.JWTHelper;
import com.CustomerSupportTicket.api.User.Entity.User;
import com.CustomerSupportTicket.api.User.Enums.Role;
import com.CustomerSupportTicket.api.User.UserService.UserService;
import com.CustomerSupportTicket.api.Utils.GeneralOperations;

@Service
public class AuthService {

    private final UserService userService;
    private final JWTHelper jwtHelper;
    private PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JWTHelper jwtHelper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtHelper = jwtHelper;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (GeneralOperations.isNullOrEmpty(request.email()) || GeneralOperations.isNullOrEmpty(request.password())) {
            throw new BadCredentialsException("No Email or Password in request");
        }
        String email = request.email().strip().toLowerCase();
        if (userService.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setFull_name(request.full_name().strip());
        user.setEmail(email);
        user.setPassword_hash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN);
        user.setDeleted(false);
        User savedUser;
        savedUser = userService.saveUser(user);
        return new RegisterResponse(savedUser.getUser_id(), savedUser.getFull_name(), savedUser.getEmail(),
                savedUser.getRole().name());
    }

    public LoginResponse login(LoginRequest request) {
        if (GeneralOperations.isNullOrEmpty(request.email()) || GeneralOperations.isNullOrEmpty(request.password())) {
            throw new BadCredentialsException("No Email or Password in request");
        }
        boolean authenticate = userService.authenticateUser(request.email(),
                passwordEncoder.encode(request.password()));
        if (authenticate) {
            ORMGetUser user = userService.getUserDetails(request.email());
            String token = jwtHelper.generateToken(user.getUser_id(), user.getEmail(), user.getRole());
            return new LoginResponse(user.getUser_id(), user.getFull_name(), user.getEmail(), user.getRole(), token,
                    "Bearer ");
        } else {
            throw new BadCredentialsException("Email or Password is incorrect!");
        }
    }
}
