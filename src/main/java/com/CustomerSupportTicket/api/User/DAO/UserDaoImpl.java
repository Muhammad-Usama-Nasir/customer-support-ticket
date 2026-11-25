package com.CustomerSupportTicket.api.User.DAO;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.CustomerSupportTicket.api.Auth.DTO.ORMGetUser;
import com.CustomerSupportTicket.api.Database.DBOperations;
import com.CustomerSupportTicket.api.GenericDTOs.StoredProcedureParameters;
import com.CustomerSupportTicket.api.User.Entity.User;

@Repository
public class UserDaoImpl implements UserDao {

    private DBOperations db;

    public UserDaoImpl(DBOperations db) {
        this.db = db;
    }

    @Override
    public boolean existsByEmail(List<StoredProcedureParameters> parameters) {
        String exists = db.storedProcedureScalar("", String.class, parameters);
        if (exists.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public User saveUser(User user) {
        return (User) db.saveEntityWithIdentity(user);
    }

    @Override
    public boolean authenticateUser(List<StoredProcedureParameters> parameters) {
        String exists = db.storedProcedureScalar("", String.class, parameters);
        if (exists.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ORMGetUser getUserDetails(List<StoredProcedureParameters> parameters) {
        List<ORMGetUser> userArr = db.StoredProcedureQuery("", ORMGetUser.class, parameters);
        if (userArr != null && userArr.size() > 0) {
            return userArr.get(0);
        } else {
            return null;
        }
    }

}
