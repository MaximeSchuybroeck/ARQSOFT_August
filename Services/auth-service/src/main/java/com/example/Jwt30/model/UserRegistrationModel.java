package com.example.Jwt30.model;

import com.example.Jwt30.entity.Role;
import com.example.Jwt30.entity.User;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class UserRegistrationModel {
    private String firstName;
    private String email;
    private String password;
    private Role role;

    public User dissemble() {
        BCryptPasswordEncoder bCryptPasswordEncoder =new BCryptPasswordEncoder();
        User user = new User();
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(this.role);
        return user;
    }
    public UserRegistrationModel assemble(User user) {
        UserRegistrationModel userRegistrationModel = new UserRegistrationModel();
        userRegistrationModel.setEmail(user.getEmail());
        userRegistrationModel.setFirstName(user.getFirstName());
        userRegistrationModel.setPassword(user.getPassword());
        return userRegistrationModel;
    }

}
