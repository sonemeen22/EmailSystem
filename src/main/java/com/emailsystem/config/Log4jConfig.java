package com.emailsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Log4jConfig {
    
    static {
        // 手动配置Log4j
        System.setProperty("log4j.rootLogger", "INFO, stdout, file");
        System.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        System.setProperty("log4j.appender.stdout.Target", "System.out");
        System.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        System.setProperty("log4j.appender.stdout.layout.ConversionPattern", 
                          "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
        
        System.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
        System.setProperty("log4j.appender.file.File", "logs/email-system.log");
        System.setProperty("log4j.appender.file.MaxFileSize", "10MB");
        System.setProperty("log4j.appender.file.MaxBackupIndex", "10");
        System.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
        System.setProperty("log4j.appender.file.layout.ConversionPattern", 
                          "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
        
        // Hibernate日志
        System.setProperty("log4j.logger.org.hibernate", "INFO");
        System.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        System.setProperty("log4j.logger.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");
        
        // Spring日志
        System.setProperty("log4j.logger.org.springframework", "INFO");
        System.setProperty("log4j.logger.com.emailsystem", "DEBUG");
    }
    
    @Bean
    public String initializeLogging() {
        // 触发静态块执行
        return "Log4j Initialized";
    }
}