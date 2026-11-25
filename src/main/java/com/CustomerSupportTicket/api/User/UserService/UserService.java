package com.CustomerSupportTicket.api.User.UserService;

import com.CustomerSupportTicket.api.Auth.DTO.ORMGetUser;
import com.CustomerSupportTicket.api.User.Entity.User;

public interface UserService {

    boolean existsByEmail(String email);

    User saveUser(User user);

    boolean authenticateUser(String email, String password);

    ORMGetUser getUserDetails(String email);

}
