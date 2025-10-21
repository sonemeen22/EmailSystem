package com.emailsystem.service;

import com.emailsystem.entity.*;
import com.emailsystem.repository.EmailRepository;
import com.emailsystem.repository.UserRepository;
import com.emailsystem.entity.EmailRecipient.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {
    
    @Autowired
    private EmailRepository emailRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Email sendEmail(Email email, List<String> toEmails, List<String> ccEmails, List<String> bccEmails) {
        email.setStatus(EmailStatus.SENT);
        email.setSendTime(LocalDateTime.now());
        
        List<EmailRecipient> recipients = new ArrayList<>();
        
        // 添加收件人
        if (toEmails != null) {
            recipients.addAll(createRecipients(email, toEmails, RecipientType.TO, "inbox"));
        }
        
        // 添加抄送
        if (ccEmails != null) {
            recipients.addAll(createRecipients(email, ccEmails, RecipientType.CC, "inbox"));
        }
        
        // 添加密送
        if (bccEmails != null) {
            recipients.addAll(createRecipients(email, bccEmails, RecipientType.BCC, "inbox"));
        }
        
        email.setRecipients(recipients);
        return emailRepository.save(email);
    }
    
    @Transactional
    public Email saveDraft(Email email, List<String> toEmails, List<String> ccEmails) {
        email.setStatus(EmailStatus.DRAFT);
        
        List<EmailRecipient> recipients = new ArrayList<>();
        
        if (toEmails != null) {
            recipients.addAll(createRecipients(email, toEmails, RecipientType.TO, "draft"));
        }
        
        if (ccEmails != null) {
            recipients.addAll(createRecipients(email, ccEmails, RecipientType.CC, "draft"));
        }
        
        email.setRecipients(recipients);
        return emailRepository.save(email);
    }
    
    private List<EmailRecipient> createRecipients(Email email, List<String> emails, RecipientType type1, String folder) {
        return emails.stream()
                .map(emailStr -> {
                    User recipient = userRepository.findByEmail(emailStr)
                            .orElseThrow(() -> new RuntimeException("用户不存在: " + emailStr));
                    
                    EmailRecipient er = new EmailRecipient();
                    er.setEmail(email);
                    er.setRecipient(recipient);
                    er.setRecipientType(type1);
                    er.setFolder(folder);
                    return er;
                })
                .collect(Collectors.toList());
    }
    
    public List<Email> getInbox(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return emailRepository.findReceivedEmails(user, "inbox");
    }
    
    public List<Email> getSentEmails(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return emailRepository.findBySenderAndStatusOrderByCreatedAtDesc(user, "SENT");
    }
    
    public List<Email> getDrafts(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return emailRepository.findBySenderAndStatusOrderByCreatedAtDesc(user, "DRAFT");
    }
    
    public List<Email> searchEmails(Integer userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return emailRepository.searchByKeyword(user, keyword);
    }
    
    @Transactional
    public void markAsRead(Integer emailId, Integer userId) {
        // 实现标记为已读逻辑
    }
    
    @Transactional
    public void markAsImportant(Integer emailId, Integer userId, boolean important) {
        // 实现标记重要邮件逻辑
    }
    
    @Transactional
    public void moveToFolder(Integer emailId, Integer userId, String folder) {
        // 实现移动邮件逻辑
    }
    
    @Transactional
    public void deleteEmails(List<Integer> emailIds, Integer userId) {
        // 实现删除邮件逻辑
    }
}
