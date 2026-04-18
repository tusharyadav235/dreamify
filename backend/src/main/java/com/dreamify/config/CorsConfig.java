package com.dreamify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Changed from /api/** to /** to cover all endpoints
                .allowedOriginPatterns(
                    "https://dreamify.info", 
                    "https://www.dreamify.info",
                    "https://*.dreamify.info", // Allows any subdomains
                    "http://localhost:[*]",      // Useful for local testing
                    "http://52.4.161.1"        
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) 
                .maxAge(3600);
    }
}
