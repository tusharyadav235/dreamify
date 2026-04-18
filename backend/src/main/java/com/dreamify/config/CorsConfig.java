package com.dreamify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // allowedOriginPatterns is REQUIRED when allowCredentials is true
                .allowedOriginPatterns(
                    "https://dreamify.info", 
                    "https://www.dreamify.info",
                    "https://*.dreamify.info",
                    "http://localhost:[*]",
                    "http://52.4.161.1"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization") // Optional: useful if you use JWTs later
                .allowCredentials(true)
                .maxAge(3600);
    }
}
