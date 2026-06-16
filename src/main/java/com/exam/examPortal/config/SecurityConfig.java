package com.exam.examPortal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF so your forms work without extra token logic for now
                .csrf(csrf -> csrf.disable())

                // 2. Allow public access to all your login/registration/asset pages
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/**", "/admin/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Disable the default Spring Security login form
                .formLogin(form -> form.disable())

                // 4. IMPORTANT: Disable HTTP Basic auth if it's popping up
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}