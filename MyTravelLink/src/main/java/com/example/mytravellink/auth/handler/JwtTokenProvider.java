package com.example.mytravellink.auth.handler;

import com.example.mytravellink.user.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail()); // 사용자 이메일을 주제로 설정
        claims.put("name", user.getName()); // 토큰에 사용자 이름 넣기
        claims.put("email", user.getEmail());

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime); // 만료 시간 설정

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // 비밀 키로 서명
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // JWT 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token); // 서명 키로 검증
            return true; // 유효한 토큰
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
            return false; // 만료된 토큰
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
            return false; // 유효하지 않은 서명
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return false; // 기타 예외
        }
    }

    // JWT에서 토큰 정보 추출
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody(); // 사용자 이메일 반환

    }

}
