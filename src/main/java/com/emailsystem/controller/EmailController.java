package com.emailsystem.controller;

import com.emailsystem.entity.Email;
import com.emailsystem.entity.User;
import com.emailsystem.service.EmailService;
import com.emailsystem.service.UserService;
import com.emailsystem.entity.EmailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "*")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, Object> request) {
        logger.info("开始处理发送邮件请求");
        long startTime = System.currentTimeMillis();

        try {
            // 记录请求基本信息
            logger.debug("发送邮件请求参数: {}", request);

            // 解析请求参数 - 从sender对象中获取userId
            Integer senderId = null;

            // 检查请求中是否包含sender对象
            if (request.containsKey("sender")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> senderMap = (Map<String, Object>) request.get("sender");
                if (senderMap != null && senderMap.containsKey("userId")) {
                    senderId = (Integer) senderMap.get("userId");
                    logger.debug("解析出发送者ID: {}", senderId);
                }
            }

            String subject = (String) request.get("subject");
            String content = (String) request.get("content");

            @SuppressWarnings("unchecked")
            List<String> toEmails = (List<String>) request.get("to");
            @SuppressWarnings("unchecked")
            List<String> ccEmails = (List<String>) request.get("cc");
            @SuppressWarnings("unchecked")
            List<String> bccEmails = (List<String>) request.get("bcc");

            logger.info("发送邮件 - 主题: {}, 收件人数量: {}, 抄送数量: {}, 密送数量: {}",
                    subject, toEmails != null ? toEmails.size() : 0,
                    ccEmails != null ? ccEmails.size() : 0,
                    bccEmails != null ? bccEmails.size() : 0);

            // 根据senderId获取用户信息
            User sender = userService.findById(senderId);
            if (sender == null) {
                logger.warn("发送者不存在, senderId: {}", senderId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "发送者不存在");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            logger.debug("找到发送者: {} ({})", sender.getUsername(), sender.getEmail());

            // 创建邮件对象并设置属性
            Email email = new Email();
            email.setSender(sender);
            email.setSubject(subject);
            email.setContent(content);
            email.setSendTime(LocalDateTime.now());
            email.setCreatedAt(LocalDateTime.now());
            email.setStatus(EmailStatus.SENT);

            logger.debug("开始调用邮件服务发送邮件");
            Email sentEmail = emailService.sendEmail(email, toEmails, ccEmails, bccEmails);
            logger.info("邮件发送成功, 邮件ID: {}", sentEmail.getEmailId());

            Map<String, Object> EmailInfo = new HashMap<>();
            EmailInfo.put("emailId", sentEmail.getEmailId());
            EmailInfo.put("subject", sentEmail.getSubject());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "发送成功");
            response.put("emailInfo", EmailInfo);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("发送邮件请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("发送邮件时发生异常", e);
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("发送邮件请求处理失败, 耗时: {}ms, 错误: {}", executionTime, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<Map<String, Object>> getInbox(@PathVariable("userId") Integer userId) {
        logger.info("开始获取收件箱, userId: {}", userId);
        long startTime = System.currentTimeMillis();

        try {
            List<Email> emails = emailService.getInbox(userId);
            logger.info("成功获取收件箱邮件, userId: {}, 邮件数量: {}", userId, emails.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "收件箱");
            response.put("emails", emails);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("获取收件箱请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取收件箱时发生异常, userId: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取收件箱失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<Email>> getSentEmails(@PathVariable Integer userId) {
        logger.info("开始获取已发送邮件, userId: {}", userId);
        long startTime = System.currentTimeMillis();

        try {
            List<Email> emails = emailService.getSentEmails(userId);
            logger.info("成功获取已发送邮件, userId: {}, 邮件数量: {}", userId, emails.size());

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("获取已发送邮件请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            logger.error("获取已发送邮件时发生异常, userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/drafts/{userId}")
    public ResponseEntity<List<Email>> getDrafts(@PathVariable Integer userId) {
        logger.info("开始获取草稿箱, userId: {}", userId);
        long startTime = System.currentTimeMillis();

        try {
            List<Email> emails = emailService.getDrafts(userId);
            logger.info("成功获取草稿箱邮件, userId: {}, 邮件数量: {}", userId, emails.size());

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("获取草稿箱请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            logger.error("获取草稿箱时发生异常, userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/{userId}")
    public ResponseEntity<List<Email>> searchEmails(
            @PathVariable Integer userId,
            @RequestParam String keyword) {
        logger.info("开始搜索邮件, userId: {}, 关键词: {}", userId, keyword);
        long startTime = System.currentTimeMillis();

        try {
            List<Email> emails = emailService.searchEmails(userId, keyword);
            logger.info("邮件搜索完成, userId: {}, 关键词: {}, 结果数量: {}", userId, keyword, emails.size());

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("邮件搜索请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            logger.error("搜索邮件时发生异常, userId: {}, keyword: {}", userId, keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markAsRead(@RequestBody Map<String, Object> request) {
        logger.info("开始处理标记已读请求");
        long startTime = System.currentTimeMillis();

        try {
            Integer emailId = (Integer) request.get("emailId");
            Integer userId = (Integer) request.get("userId");

            logger.debug("标记邮件为已读, emailId: {}, userId: {}", emailId, userId);

            emailService.markAsRead(emailId, userId);
            logger.info("成功标记邮件为已读, emailId: {}, userId: {}", emailId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "标记为已读");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("标记已读请求处理完成, 耗时: {}ms", executionTime);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("标记邮件为已读时发生异常", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "标记为已读失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}