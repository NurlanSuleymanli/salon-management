package com.nurlansuleymanli.salonmanager.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public io.swagger.v3.oas.models.OpenAPI openAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-key",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearer-key"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**", "/error",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}/make-admin").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/salons/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/salons/{id}/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/salons/{id}/delete").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/services/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/services/{id}/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/{id}/delete").hasRole("ADMIN")
                        
                        .requestMatchers(HttpMethod.POST, "/api/barbers/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/barbers/{id}/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/barbers/{id}/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/barbers/{id}/status").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/working-hours/barber/**").permitAll()
                        .requestMatchers("/api/working-hours/**").hasRole("BARBER")

                        .requestMatchers(HttpMethod.GET, "/api/reservations/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/barber-schedule").hasAnyRole("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/{id}/status").hasAnyRole("ADMIN", "BARBER")



                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}