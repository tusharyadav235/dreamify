package com.dreamify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Using specific origins is better for production security
                .allowedOrigins(
                    "https://dreamify.info", 
                    "https://www.dreamify.info",
                    "http://52.4.161.1" 
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // allowCredentials(true) is required if you ever add 
                // Cookies or JWT Auth later
                .allowCredentials(true) 
                .maxAge(3600);
    }
}
