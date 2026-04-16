package com.nurlansuleymanli.salonmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Bütün API endpointlərinə icazə ver
                .allowedOrigins(
                        "http://localhost:63342", // Sənin local testin üçün (IntelliJ)
                        "http://127.0.0.1:5500", // Sənin local testin üçün (VS Code Live Server)
                        "https://salon-management-nu.vercel.app" // 🟢 Bura Vercel URL-ini yazırsan!
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Əgər gələcəkdə cookie falan lazım olsa
    }
}
