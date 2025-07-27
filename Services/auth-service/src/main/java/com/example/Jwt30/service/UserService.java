package com.example.Jwt30.service;

import com.example.Jwt30.entity.User;
import com.example.Jwt30.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public List<User> getUser(){
        return userRepository.findAll();
    }
    public User createUser(User user){
        return userRepository.save(user);
    }

}
