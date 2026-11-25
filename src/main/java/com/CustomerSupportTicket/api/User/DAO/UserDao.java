package com.CustomerSupportTicket.api.User.DAO;

import java.util.List;

import com.CustomerSupportTicket.api.Auth.DTO.ORMGetUser;
import com.CustomerSupportTicket.api.GenericDTOs.StoredProcedureParameters;
import com.CustomerSupportTicket.api.User.Entity.User;

public interface UserDao {

    boolean existsByEmail(List<StoredProcedureParameters> parameters);

    User saveUser(User user);

    boolean authenticateUser(List<StoredProcedureParameters> parameters);

    ORMGetUser getUserDetails(List<StoredProcedureParameters> parameters);

}