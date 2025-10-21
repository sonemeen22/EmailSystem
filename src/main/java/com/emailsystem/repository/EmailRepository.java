package com.emailsystem.repository;

import com.emailsystem.entity.Email;
import com.emailsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Integer> {
    
    List<Email> findBySenderAndStatusOrderByCreatedAtDesc(User sender, String status);
    
    @Query("SELECT e FROM Email e JOIN e.recipients r WHERE r.recipient = :user AND r.folder = :folder ORDER BY e.createdAt DESC")
    List<Email> findReceivedEmails(@Param("user") User user, @Param("folder") String folder);
    
    @Query("SELECT e FROM Email e WHERE e.sender = :user AND (e.subject LIKE %:keyword% OR e.content LIKE %:keyword%)")
    List<Email> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);
}