package com.mca.project.securedocsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect / → /login for anonymous users
        registry.addRedirectViewController("/", "/login");

        // Map /login and /dashboard to templates (login.html, dashboard.html)
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/dashboard").setViewName("dashboard");

        // Optional: direct mappings for upload/search if you have only templates
        registry.addViewController("/upload").setViewName("upload");
        registry.addViewController("/search").setViewName("search");
    }
}
