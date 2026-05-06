package com.allergen.security;

import com.allergen.exception.UnauthorizedException;
import com.allergen.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    public static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    private final SecretKey secretKey;
    private final long expirationMs;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public JwtService(@Value("${app.jwt.secret:allergen-system-dev-secret-key-32-bytes-min}") String secret,
                      @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
                      @Value("${app.jwt.cookie.secure:false}") boolean cookieSecure,
                      @Value("${app.jwt.cookie.same-site:Lax}") String cookieSameSite) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        return parseToken(token);
    }

    public Long extractUserId(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            return extractUserId(authorizationHeader);
        }

        String cookieToken = extractTokenFromCookie(request);
        if (cookieToken == null || cookieToken.isBlank()) {
            throw new UnauthorizedException("Authorization token is required");
        }

        return parseToken(cookieToken);
    }

    public String extractRole(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token;
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            token = extractBearerToken(authorizationHeader);
        } else {
            token = extractTokenFromCookie(request);
            if (token == null || token.isBlank()) {
                throw new UnauthorizedException("Authorization token is required");
            }
        }
        return parseRole(token);
    }

    public ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(expirationMs / 1000)
                .sameSite(cookieSameSite)
                .build();
    }

    public ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    /**
     * Parses JWT from Authorization header or cookie and populates {@link SecurityContextHolder}
     * when the token is valid. No-op when absent; does not throw on invalid token.
     */
    public void resolveAuthenticationFromRequest(HttpServletRequest request) {
        String token = extractRawToken(request);
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.parseLong(claims.getSubject());
            String role = claims.get("role", String.class);
            if (role == null || role.isBlank()) {
                role = "USER";
            }
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            // Leave unauthenticated; public endpoints remain reachable without a token.
        }
    }

    private String extractRawToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return extractTokenFromCookie(request);
    }

    private Long parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    private String parseRole(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return claims.get("role", String.class);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authorization token is required");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must use Bearer token");
        }
        return authorizationHeader.substring(7).trim();
    }
}
