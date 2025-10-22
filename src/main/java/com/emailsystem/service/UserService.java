package com.emailsystem.service;

import com.emailsystem.entity.User;
import com.emailsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        logger.info("开始用户注册流程，用户名: {}, 邮箱: {}", user.getUsername(), user.getEmail());

        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("用户注册失败：用户名已存在 - {}", user.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("用户注册失败：邮箱已存在 - {}", user.getEmail());
            throw new RuntimeException("邮箱已存在");
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus(1);

            User savedUser = userRepository.save(user);
            logger.info("用户注册成功，用户ID: {}, 用户名: {}", savedUser.getUserId(), savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("用户注册过程中发生异常，用户名: {}, 错误信息: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("用户注册失败", e);
        }
    }

    public Optional<User> login(String username, String password) {
        logger.info("用户登录尝试，用户名: {}", username);

        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
                User user = userOpt.get();
                user.setLastLoginTime(LocalDateTime.now());
                userRepository.save(user);

                logger.info("用户登录成功，用户ID: {}, 用户名: {}", user.getUserId(), user.getUsername());
                return Optional.of(user);
            } else {
                logger.warn("用户登录失败，用户名: {} - 用户名或密码错误", username);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("用户登录过程中发生异常，用户名: {}, 错误信息: {}", username, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        logger.info("开始修改密码流程，用户ID: {}", userId);

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);

                    logger.info("密码修改成功，用户ID: {}", userId);
                    return true;
                } else {
                    logger.warn("密码修改失败，用户ID: {} - 旧密码不正确", userId);
                    return false;
                }
            } else {
                logger.warn("密码修改失败，用户ID: {} - 用户不存在", userId);
                return false;
            }
        } catch (Exception e) {
            logger.error("修改密码过程中发生异常，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    public User updateProfile(Integer userId, User userDetails) {
        logger.info("开始更新用户资料，用户ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("更新用户资料失败，用户ID: {} - 用户不存在", userId);
                        return new RuntimeException("用户不存在");
                    });

            logger.debug("更新前用户资料 - 部门: {}, 职位: {}, 邮箱: {}",
                    user.getDepartment(), user.getPosition(), user.getEmail());

            user.setDepartment(userDetails.getDepartment());
            user.setPosition(userDetails.getPosition());
            user.setEmail(userDetails.getEmail());
            user.setUpdatedAt(LocalDateTime.now());

            User updatedUser = userRepository.save(user);
            logger.info("用户资料更新成功，用户ID: {}", userId);
            logger.debug("更新后用户资料 - 部门: {}, 职位: {}, 邮箱: {}",
                    updatedUser.getDepartment(), updatedUser.getPosition(), updatedUser.getEmail());

            return updatedUser;
        } catch (Exception e) {
            logger.error("更新用户资料过程中发生异常，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}