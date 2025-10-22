package com.emailsystem.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // 返回空数组，所有配置都在 AppConfig 中
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // 只返回 AppConfig 类，所有配置都在这里
        return new Class[] { AppConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // 文件上传配置
        registration.setMultipartConfig(new MultipartConfigElement("", 52428800, 52428800, 0));
    }
}