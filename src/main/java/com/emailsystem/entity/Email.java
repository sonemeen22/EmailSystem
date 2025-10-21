package com.emailsystem.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer emailId;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(nullable = false, length = 500)
    private String subject;
    
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    private LocalDateTime sendTime;
    
    @Enumerated(EnumType.STRING)
    private EmailStatus status;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
    private List<EmailRecipient> recipients;
    
    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
    private List<EmailAttachment> attachments;
    
    // getters and setters
    public Integer getEmailId() { return emailId; }
    public void setEmailId(Integer emailId) { this.emailId = emailId; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    public EmailStatus getStatus() { return status; }
    public void setStatus(EmailStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<EmailRecipient> getRecipients() { return recipients; }
    public void setRecipients(List<EmailRecipient> recipients) { this.recipients = recipients; }
    public List<EmailAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<EmailAttachment> attachments) { this.attachments = attachments; }
}

