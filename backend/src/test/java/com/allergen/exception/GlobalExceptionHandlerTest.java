package com.allergen.exception;

import com.allergen.controller.AllergenController;
import com.allergen.controller.AuthController;
import com.allergen.dto.auth.UserProfileAssembler;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import com.allergen.service.AllergenService;
import com.allergen.service.AvatarImageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

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
    private ResourceBundleMessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.extractUserId(any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenThrow(new UnauthorizedException("error.auth.tokenRequired"));

        AuthController authController = new AuthController(
                mock(UserRepository.class),
                jwtService,
                mock(AvatarImageService.class),
                new UserProfileAssembler(),
                messageSource
        );
        authMockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .setValidator(validator)
                .build();

        AllergenController allergenController = new AllergenController(mock(AllergenService.class), jwtService, messageSource);
        allergenMockMvc = MockMvcBuilders
                .standaloneSetup(allergenController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
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
    void registerReturnsLocalizedValidationErrorForRussianLocale() throws Exception {
        authMockMvc.perform(post("/api/auth/register")
                        .header("Accept-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }

    @Test
    void historyReturnsUnauthorizedWhenAuthorizationHeaderMissing() throws Exception {
        allergenMockMvc.perform(get("/api/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization token is required"));
    }

    @Test
    void historyReturnsKyrgyzWhenLocaleIsKyrgyz() throws Exception {
        allergenMockMvc.perform(get("/api/history").locale(Locale.forLanguageTag("kg")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Авторизация токени талап кылынат"));
    }

    @Test
    void unsupportedLocaleFallsBackToEnglish() throws Exception {
        allergenMockMvc.perform(get("/api/history").header("Accept-Language", "de"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization token is required"));
    }
}


