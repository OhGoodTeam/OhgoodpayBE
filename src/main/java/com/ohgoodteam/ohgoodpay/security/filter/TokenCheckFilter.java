package com.ohgoodteam.ohgoodpay.security.filter;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ohgoodteam.ohgoodpay.security.exception.AccessTokenException;
import com.ohgoodteam.ohgoodpay.security.util.JWTUtil;

import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenCheckFilter extends OncePerRequestFilter {

    private JWTUtil jwtUtil;

    public TokenCheckFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 토큰 검증
     * @param request 요청
     * @param response 응답
     * @param filterChain 필터 체인
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api")) { // /api 주소가 아니면(일반접속이면) 통과
            filterChain.doFilter(request, response);
            return;
        }
        
        // /api/public/** 경로는 토큰 검증 제외
        // + image-proxy의 경우도 검증 제외해야 해서 추가
        // + swagger 설정 추가
        if (path.startsWith("/api/public/") || path.startsWith("/api/image-proxy") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        // AccessToken 검증
        try {
            Map<String, Object> claims = validateAccessToken(request);
            
            // SecurityContext에 인증 정보 설정
            String customerId = (String) claims.get("customerId");
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customerId, 
                null, 
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            filterChain.doFilter(request, response);
        } catch (AccessTokenException e) {
            // e.printStackTrace();
            e.sendResponseError(response);
        } catch (Exception e) {
            // e.printStackTrace();
            // 기타 예외는 일반적인 401 에러로 처리
            AccessTokenException accessTokenException = new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
            accessTokenException.sendResponseError(response);
        }
    }

    /**
     * 토큰 값 검증 (페이로드 반환)
     * @param request
     * @return
     * @throws AccessTokenException
     */
    private Map<String, Object> validateAccessToken(HttpServletRequest request) throws AccessTokenException {
        String headerStr = request.getHeader("Authorization");
        if (headerStr == null || headerStr.length() < 8) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }
        // Bearer 생략
        String tokenType = headerStr.substring(0,6);
        String tokenStr = headerStr.substring(7);

        if (tokenType.equalsIgnoreCase("Bearer") == false) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        try {
            Map<String, Object> value = jwtUtil.validateToken(tokenStr);
            return value;
        } catch (Exception e) {
            // JWT 검증 실패 시 만료된 토큰으로 간주
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }
    }
}
