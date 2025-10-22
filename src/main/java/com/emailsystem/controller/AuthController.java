package com.emailsystem.controller;

import com.emailsystem.entity.User;
import com.emailsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        logger.info("尝试登录用户: {}", username);

        // 记录登录请求但不记录密码
        logger.debug("登录请求 - 用户名: {}, 密码长度: {}", username,
                credentials.get("password") != null ? credentials.get("password").length() : 0);

        String password = credentials.get("password");

        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("用户登录成功: {} (ID: {})", username, user.getUserId());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", userInfo);

            logger.info("response: {}", response);
            return ResponseEntity.ok(response);
        } else {
            logger.warn("用户登录失败: {} - 用户名或密码错误", username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("尝试注册新用户: {}", user.getUsername());
        logger.debug("注册用户详细信息: {}", user.toString());

        try {
            User registeredUser = userService.register(user);
            logger.info("用户注册成功: {} (ID: {})", registeredUser.getUsername(), registeredUser.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", registeredUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("用户注册失败: {} - {}", user.getUsername(), e.getMessage());
            logger.debug("注册失败异常详情:", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        Integer userId = Integer.parseInt(request.get("userId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        logger.info("尝试修改密码 - 用户ID: {}", userId);
        logger.debug("密码修改请求 - 用户ID: {}, 原密码长度: {}, 新密码长度: {}",
                userId,
                oldPassword != null ? oldPassword.length() : 0,
                newPassword != null ? newPassword.length() : 0);

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        if (success) {
            logger.info("密码修改成功 - 用户ID: {}", userId);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("密码修改失败 - 用户ID: {} - 原密码错误", userId);
            response.put("message", "原密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("认证控制器发生未处理异常: {}", e.getMessage());
        logger.debug("异常堆栈信息:", e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "系统内部错误");
        return ResponseEntity.internalServerError().body(response);
    }
}