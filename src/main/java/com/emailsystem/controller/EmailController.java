package com.emailsystem.controller;

import com.emailsystem.entity.Email;
import com.emailsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "*")
public class EmailController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, Object> request) {
        try {
            // 解析请求并发送邮件
            Email email = new Email();
            // 设置邮件属性...
            
            @SuppressWarnings("unchecked")
            List<String> toEmails = (List<String>) request.get("to");
            @SuppressWarnings("unchecked")
            List<String> ccEmails = (List<String>) request.get("cc");
            @SuppressWarnings("unchecked")
            List<String> bccEmails = (List<String>) request.get("bcc");
            
            Email sentEmail = emailService.sendEmail(email, toEmails, ccEmails, bccEmails);
            return ResponseEntity.ok(sentEmail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<Email>> getInbox(@PathVariable Integer userId) {
        List<Email> emails = emailService.getInbox(userId);
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<Email>> getSentEmails(@PathVariable Integer userId) {
        List<Email> emails = emailService.getSentEmails(userId);
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/drafts/{userId}")
    public ResponseEntity<List<Email>> getDrafts(@PathVariable Integer userId) {
        List<Email> emails = emailService.getDrafts(userId);
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/search/{userId}")
    public ResponseEntity<List<Email>> searchEmails(
            @PathVariable Integer userId,
            @RequestParam String keyword) {
        List<Email> emails = emailService.searchEmails(userId, keyword);
        return ResponseEntity.ok(emails);
    }
    
    @PostMapping("/mark-read")
    public ResponseEntity<?> markAsRead(@RequestBody Map<String, Object> request) {
        Integer emailId = (Integer) request.get("emailId");
        Integer userId = (Integer) request.get("userId");
        emailService.markAsRead(emailId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);  // 添加success字段
        response.put("message", "标记为已读");
        return ResponseEntity.ok(response);
    }
}