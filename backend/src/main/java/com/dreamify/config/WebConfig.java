package com.dreamify.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc // Explicitly tells Spring to evaluate custom configurations with high priority
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    // ── 1. UNIFIED CORS POLICY MAPPINGS ──
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://dreamify.info",
                    "https://www.dreamify.info",
                    "https://*.dreamify.info",
                    "http://localhost:*",
                    "http://52.4.161.1"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ── 2. STATIC MEDIA CONTAINER VOLUME RESOURCES ──
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/app/uploads/")
                .setCachePeriod(86400); // 1 Day client retention cache
    }

    // ── 3. ADMINISTRATIVE INTERCEPTOR ROUTING RULES ──
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                    "/api/enquiries", "/api/enquiries/**",
                    "/api/testimonials", "/api/testimonials/**",
                    "/api/video-testimonials", "/api/video-testimonials/**"
                )
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/enquiries/submit",
                    "/api/testimonials",
                    "/api/video-testimonials" // Exclude base public video GET requests too!
                );
    }
}
