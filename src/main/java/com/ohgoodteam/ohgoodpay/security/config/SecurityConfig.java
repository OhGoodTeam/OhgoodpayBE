package com.ohgoodteam.ohgoodpay.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import com.ohgoodteam.ohgoodpay.config.CorsConfig;
import com.ohgoodteam.ohgoodpay.security.filter.ApiLoginFilter;
import com.ohgoodteam.ohgoodpay.security.filter.ApiLoginSuccessHandler;
import com.ohgoodteam.ohgoodpay.security.filter.TokenCheckFilter;
import com.ohgoodteam.ohgoodpay.security.service.ApiCustomerDetailsService;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    
    private final JWTUtil jwtUtil;
    private final CorsConfig corsConfig;
    private final ApiCustomerDetailsService apiCustomerDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 이미지 프록시 처리를 위해 "/api/image-proxy" 추가.
        // /swagger-ui/** 를 permitAll
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/api/public/**","http://localhost:8000/ml/**", "/api/image-proxy/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html","/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // OPTIONS 요청 허용
            .anyRequest().authenticated());
        
        http.csrf(csrf -> csrf.disable());

        http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()));

        // 기본 로그인 페이지 리다이렉트 비활성화
        http.formLogin(login -> login.disable());
        http.logout(logout -> logout.disable());
        
        // 인증 실패 시 리다이렉트 대신 401 에러 반환
        http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
        }));

        // JWT 관련 설정
		// AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder = http
        .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(apiCustomerDetailsService).passwordEncoder(passwordEncoder());

        // AuthenticationManager 객체 생성
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.authenticationManager(authenticationManager);

        // 필터
		ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth"); // 토큰발급URL (http://localhost:8080/auth)
		apiLoginFilter.setAuthenticationManager(authenticationManager);
		apiLoginFilter.setAuthenticationSuccessHandler(new ApiLoginSuccessHandler(jwtUtil));

        // 필터동작위치
		http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);

        // 토큰체크필터
        TokenCheckFilter tokenCheckFilter = new TokenCheckFilter(jwtUtil);
		http.addFilterBefore(tokenCheckFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
