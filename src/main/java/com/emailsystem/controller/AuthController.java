package com.emailsystem.controller;

import com.emailsystem.entity.User;
import com.emailsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "登录成功");
            response.put("user", userOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        Integer userId = Integer.parseInt(request.get("userId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (success) {
            return ResponseEntity.ok("密码修改成功");
        } else {
            return ResponseEntity.badRequest().body("原密码错误");
        }
    }
}