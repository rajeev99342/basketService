package com.service.service;

import com.service.entities.User;
import com.service.model.UserCredentials;
import com.service.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;
    public void saveUserDetails(UserCredentials userCredentials){
        User user = new User();
        user.setUserName(userCredentials.getName());
        user.setPhone(userCredentials.getMobile());
        user.setPassword(userCredentials.getPassword());
        userRepo.save(user);
    }
}
