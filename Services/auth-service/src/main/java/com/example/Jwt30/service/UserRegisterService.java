package com.example.Jwt30.service;

import com.example.Jwt30.entity.User;
import com.example.Jwt30.model.UserRegistrationModel;
import com.example.Jwt30.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserRegisterService {
    @Autowired
    private UserRepository userRepository;

    public String saveuser(UserRegistrationModel userRegistrationModel) {
        String incomingEmail = userRegistrationModel.getEmail();
        User userExist = userRepository.findByEmail(incomingEmail);
        if (userExist != null) {
            return "User already exist with same email id!";
        } else {

            User user = new User();
            user = userRegistrationModel.dissemble();
            userRepository.save(user);
            return "User saved successfully!";
        }
    }
}
