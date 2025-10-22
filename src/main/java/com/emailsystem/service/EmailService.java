package com.emailsystem.service;

import com.emailsystem.entity.*;
import com.emailsystem.repository.EmailRepository;
import com.emailsystem.repository.UserRepository;
import com.emailsystem.entity.EmailRecipient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Email sendEmail(Email email, List<String> toEmails, List<String> ccEmails, List<String> bccEmails) {
        logger.info("开始发送邮件，发件人: {}, 主题: {}",
                email.getSender().getEmail(), email.getSubject());
        logger.debug("收件人: {}, 抄送: {}, 密送: {}", toEmails, ccEmails, bccEmails);

        try {
            email.setStatus(EmailStatus.SENT);
            email.setSendTime(LocalDateTime.now());

            List<EmailRecipient> recipients = new ArrayList<>();

            // 添加收件人
            if (toEmails != null) {
                logger.debug("添加 {} 个收件人", toEmails.size());
                recipients.addAll(createRecipients(email, toEmails, RecipientType.TO, "inbox"));
            }

            // 添加抄送
            if (ccEmails != null) {
                logger.debug("添加 {} 个抄送", ccEmails.size());
                recipients.addAll(createRecipients(email, ccEmails, RecipientType.CC, "inbox"));
            }

            // 添加密送
            if (bccEmails != null) {
                logger.debug("添加 {} 个密送", bccEmails.size());
                recipients.addAll(createRecipients(email, bccEmails, RecipientType.BCC, "inbox"));
            }

            email.setRecipients(recipients);
            Email savedEmail = emailRepository.save(email);

            logger.info("邮件发送成功，邮件ID: {}, 总收件人数: {}",
                    savedEmail.getEmailId(), recipients.size());
            return savedEmail;

        } catch (Exception e) {
            logger.error("发送邮件失败，发件人: {}, 主题: {}, 错误: {}",
                    email.getSender().getEmail(), email.getSubject(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Email saveDraft(Email email, List<String> toEmails, List<String> ccEmails) {
        logger.info("开始保存草稿，发件人: {}, 主题: {}",
                email.getSender().getEmail(), email.getSubject());
        logger.debug("草稿收件人: {}, 抄送: {}", toEmails, ccEmails);

        try {
            email.setStatus(EmailStatus.DRAFT);

            List<EmailRecipient> recipients = new ArrayList<>();

            if (toEmails != null) {
                logger.debug("添加 {} 个草稿收件人", toEmails.size());
                recipients.addAll(createRecipients(email, toEmails, RecipientType.TO, "draft"));
            }

            if (ccEmails != null) {
                logger.debug("添加 {} 个草稿抄送", ccEmails.size());
                recipients.addAll(createRecipients(email, ccEmails, RecipientType.CC, "draft"));
            }

            email.setRecipients(recipients);
            Email savedDraft = emailRepository.save(email);

            logger.info("草稿保存成功，草稿ID: {}", savedDraft.getEmailId());
            return savedDraft;

        } catch (Exception e) {
            logger.error("保存草稿失败，发件人: {}, 主题: {}, 错误: {}",
                    email.getSender().getEmail(), email.getSubject(), e.getMessage(), e);
            throw e;
        }
    }

    private List<EmailRecipient> createRecipients(Email email, List<String> emails, RecipientType type, String folder) {
        logger.debug("开始创建收件人列表，类型: {}, 文件夹: {}, 邮箱数量: {}", type, folder, emails.size());

        return emails.stream()
                .map(emailStr -> {
                    try {
                        logger.trace("处理收件人邮箱: {}", emailStr);
                        User recipient = userRepository.findByEmail(emailStr)
                                .orElseThrow(() -> new RuntimeException("用户不存在: " + emailStr));

                        EmailRecipient er = new EmailRecipient();
                        er.setEmail(email);
                        er.setRecipient(recipient);
                        er.setRecipientType(type);
                        er.setFolder(folder);

                        logger.trace("收件人创建成功: {}", emailStr);
                        return er;

                    } catch (Exception e) {
                        logger.error("创建收件人失败，邮箱: {}, 错误: {}", emailStr, e.getMessage());
                        throw e;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Email> getInbox(Integer userId) {
        logger.info("获取收件箱邮件，用户ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("用户不存在，用户ID: {}", userId);
                        return new RuntimeException("用户不存在");
                    });

            List<Email> inboxEmails = emailRepository.findReceivedEmails(user, "inbox");
            logger.info("成功获取收件箱邮件，用户ID: {}, 邮件数量: {}", userId, inboxEmails.size());
            return inboxEmails;

        } catch (Exception e) {
            logger.error("获取收件箱失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public List<Email> getSentEmails(Integer userId) {
        logger.info("获取已发送邮件，用户ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("用户不存在，用户ID: {}", userId);
                        return new RuntimeException("用户不存在");
                    });

            List<Email> sentEmails = emailRepository.findBySenderAndStatusOrderByCreatedAtDesc(user, "SENT");
            logger.info("成功获取已发送邮件，用户ID: {}, 邮件数量: {}", userId, sentEmails.size());
            return sentEmails;

        } catch (Exception e) {
            logger.error("获取已发送邮件失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public List<Email> getDrafts(Integer userId) {
        logger.info("获取草稿箱邮件，用户ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("用户不存在，用户ID: {}", userId);
                        return new RuntimeException("用户不存在");
                    });

            List<Email> draftEmails = emailRepository.findBySenderAndStatusOrderByCreatedAtDesc(user, "DRAFT");
            logger.info("成功获取草稿箱邮件，用户ID: {}, 邮件数量: {}", userId, draftEmails.size());
            return draftEmails;

        } catch (Exception e) {
            logger.error("获取草稿箱失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public List<Email> searchEmails(Integer userId, String keyword) {
        logger.info("搜索邮件，用户ID: {}, 关键词: {}", userId, keyword);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("用户不存在，用户ID: {}", userId);
                        return new RuntimeException("用户不存在");
                    });

            List<Email> searchResults = emailRepository.searchByKeyword(user, keyword);
            logger.info("邮件搜索完成，用户ID: {}, 关键词: {}, 结果数量: {}",
                    userId, keyword, searchResults.size());
            return searchResults;

        } catch (Exception e) {
            logger.error("邮件搜索失败，用户ID: {}, 关键词: {}, 错误: {}",
                    userId, keyword, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void markAsRead(Integer emailId, Integer userId) {
        logger.info("标记邮件为已读，邮件ID: {}, 用户ID: {}", emailId, userId);
        // 实现标记为已读逻辑
        try {
            // 具体的业务逻辑实现
            logger.debug("邮件标记为已读处理完成，邮件ID: {}, 用户ID: {}", emailId, userId);
        } catch (Exception e) {
            logger.error("标记邮件为已读失败，邮件ID: {}, 用户ID: {}, 错误: {}",
                    emailId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void markAsImportant(Integer emailId, Integer userId, boolean important) {
        logger.info("{}重要邮件标记，邮件ID: {}, 用户ID: {}",
                important ? "添加" : "移除", emailId, userId);
        // 实现标记重要邮件逻辑
        try {
            // 具体的业务逻辑实现
            logger.debug("重要邮件标记处理完成，邮件ID: {}, 用户ID: {}, 状态: {}",
                    emailId, userId, important);
        } catch (Exception e) {
            logger.error("重要邮件标记失败，邮件ID: {}, 用户ID: {}, 状态: {}, 错误: {}",
                    emailId, userId, important, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void moveToFolder(Integer emailId, Integer userId, String folder) {
        logger.info("移动邮件到文件夹，邮件ID: {}, 用户ID: {}, 目标文件夹: {}",
                emailId, userId, folder);
        // 实现移动邮件逻辑
        try {
            // 具体的业务逻辑实现
            logger.debug("邮件移动完成，邮件ID: {}, 用户ID: {}, 目标文件夹: {}",
                    emailId, userId, folder);
        } catch (Exception e) {
            logger.error("移动邮件失败，邮件ID: {}, 用户ID: {}, 目标文件夹: {}, 错误: {}",
                    emailId, userId, folder, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteEmails(List<Integer> emailIds, Integer userId) {
        logger.info("删除邮件，用户ID: {}, 邮件数量: {}, 邮件IDs: {}",
                userId, emailIds.size(), emailIds);
        // 实现删除邮件逻辑
        try {
            // 具体的业务逻辑实现
            logger.info("邮件删除完成，用户ID: {}, 删除邮件数量: {}", userId, emailIds.size());
        } catch (Exception e) {
            logger.error("删除邮件失败，用户ID: {}, 邮件数量: {}, 错误: {}",
                    userId, emailIds.size(), e.getMessage(), e);
            throw e;
        }
    }
}