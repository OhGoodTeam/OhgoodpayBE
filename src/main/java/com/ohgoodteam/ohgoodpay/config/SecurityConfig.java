//package com.ohgoodteam.ohgoodpay.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private static final String[] SWAGGER_WHITELIST = {
//            "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml",
//            "/swagger-resources/**", "/webjars/**"
//    };
//
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(csrf -> csrf.ignoringRequestMatchers(SWAGGER_WHITELIST).disable())
//                .cors(Customizer.withDefaults()) // ← CorsConfig bean 적용
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(SWAGGER_WHITELIST).permitAll() // ★ 요게 핵심
//                        .requestMatchers("/api/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                .build();
//    }
//}
