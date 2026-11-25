package com.CustomerSupportTicket.api.User.UserService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.CustomerSupportTicket.api.Auth.DTO.ORMGetUser;
import com.CustomerSupportTicket.api.GenericDTOs.StoredProcedureParameters;
import com.CustomerSupportTicket.api.User.DAO.UserDao;
import com.CustomerSupportTicket.api.User.Entity.User;
import com.CustomerSupportTicket.api.Utils.GeneralOperations;

@Service
public class UserServiceImpl implements UserService {

    private UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean existsByEmail(String email) {
        List<StoredProcedureParameters> parameters = new ArrayList<>();
        parameters.add(new StoredProcedureParameters("email", email, String.class));
        return userDao.existsByEmail(parameters);
    }

    @Override
    public User saveUser(User user) {
        user.setModified_at(GeneralOperations.getCurrentDateTime());
        user.setCreated_at(GeneralOperations.getCurrentDateTime());
        return userDao.saveUser(user);
    }

    @Override
    public boolean authenticateUser(String email, String password) {
        List<StoredProcedureParameters> parameters = new ArrayList<>();
        parameters.add(new StoredProcedureParameters("email", email, String.class));
        parameters.add(new StoredProcedureParameters("password", password, String.class));
        return userDao.authenticateUser(parameters);
    }

    @Override
    public ORMGetUser getUserDetails(String email) {
        List<StoredProcedureParameters> parameters = new ArrayList<>();
        parameters.add(new StoredProcedureParameters("email", email, String.class));
        return userDao.getUserDetails(parameters);
    }

}
