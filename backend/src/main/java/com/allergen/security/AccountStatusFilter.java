package com.allergen.security;

import com.allergen.dto.common.ApiErrorResponse;
import com.allergen.model.User;
import com.allergen.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * After JWT authentication, rejects removed/blocked accounts for protected API calls.
 */
@Component
public class AccountStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AccountStatusFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principalStr) {
            if (!"anonymousUser".equals(principalStr)) {
                try {
                    long userId = Long.parseLong(principalStr);
                    User user = userRepository.findById(userId).orElse(null);
                    if (user == null || user.getRemovedAt() != null) {
                        SecurityContextHolder.clearContext();
                        sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Account not found");
                        return;
                    }
                    if (user.getBlockedAt() != null) {
                        SecurityContextHolder.clearContext();
                        sendJson(response, HttpServletResponse.SC_FORBIDDEN, "Account is blocked");
                        return;
                    }
                } catch (NumberFormatException ignored) {
                    // Non-numeric principal: ignore
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(message));
    }
}
