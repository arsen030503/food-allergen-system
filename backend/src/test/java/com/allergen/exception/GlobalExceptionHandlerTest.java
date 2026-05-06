package com.allergen.exception;

import com.allergen.controller.AllergenController;
import com.allergen.controller.AuthController;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import com.allergen.service.AllergenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc authMockMvc;
    private MockMvc allergenMockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.extractUserId(any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenThrow(new UnauthorizedException("Authorization token is required"));

        AuthController authController = new AuthController(mock(UserRepository.class), jwtService);
        authMockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        AllergenController allergenController = new AllergenController(mock(AllergenService.class), jwtService);
        allergenMockMvc = MockMvcBuilders
                .standaloneSetup(allergenController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerReturnsValidationErrorForEmptyBody() throws Exception {
        authMockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void historyReturnsUnauthorizedWhenAuthorizationHeaderMissing() throws Exception {
        allergenMockMvc.perform(get("/api/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization token is required"));
    }
}


