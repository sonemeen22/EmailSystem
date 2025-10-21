package com.emailsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;
    
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;
    
    @Column(name = "read_status")
    private Integer readStatus = 0;
    
    @Column(name = "important_flag")
    private Integer importantFlag = 0;
    
    @Column(name = "folder")
    private String folder = "inbox";
    
    @Column(name = "deleted")
    private Integer deleted = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public RecipientType getRecipientType() { return recipientType; }
    public void setRecipientType(RecipientType recipientType) { this.recipientType = recipientType; }
    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }
    public Integer getImportantFlag() { return importantFlag; }
    public void setImportantFlag(Integer importantFlag) { this.importantFlag = importantFlag; }
    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

