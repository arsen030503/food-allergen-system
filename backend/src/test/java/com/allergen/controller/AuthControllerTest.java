package com.allergen.controller;

import com.allergen.dto.auth.AuthRegisterResponse;
import com.allergen.dto.auth.UpdateAvatarRequest;
import com.allergen.dto.auth.RegisterRequest;
import com.allergen.dto.auth.UpdateAllergensRequest;
import com.allergen.dto.auth.UserProfileAssembler;
import com.allergen.dto.common.MessageResponse;
import com.allergen.model.User;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import com.allergen.service.AvatarImageService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AvatarImageService avatarImageService;

    private final UserProfileAssembler userProfileAssembler = new UserProfileAssembler();
    private final ResourceBundleMessageSource messageSource = messageSource();

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    private AuthController controller() {
        return new AuthController(userRepository, jwtService, avatarImageService, userProfileAssembler, messageSource);
    }

    @Test
    void registerNormalizesEmailBeforeDuplicateCheckAndSave() {
        AuthController controller = controller();

        when(userRepository.existsByEmailAndRemovedAtIsNull("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("token-123");
        when(jwtService.buildAuthCookie("token-123")).thenReturn(
                ResponseCookie.from("AUTH_TOKEN", "token-123").path("/").build()
        );

        RegisterRequest request = new RegisterRequest();
        request.setFullName(" Test User ");
        request.setEmail(" User@Example.com ");
        request.setPassword("secret123");

        ResponseEntity<?> response = controller.register(request, Locale.ENGLISH);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        AuthRegisterResponse body = assertInstanceOf(AuthRegisterResponse.class, response.getBody());
        assertEquals("Registration successful", body.getMessage());
        verify(userRepository).existsByEmailAndRemovedAtIsNull("user@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
        assertEquals("Test User", userCaptor.getValue().getFullName());
    }

    @Test
    void updateAllergensUsesEmptyStringWhenMissingInPayload() {
        AuthController controller = controller();
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);

        User user = new User();
        user.setId(5L);
        user.setMyAllergens("Milk");

        when(userRepository.findByIdAndRemovedAtIsNull(5L)).thenReturn(Optional.of(user));
        when(jwtService.extractUserId(servletRequest)).thenReturn(5L);

        UpdateAllergensRequest payload = new UpdateAllergensRequest();
        ResponseEntity<?> response = controller.updateAllergens(5L, servletRequest, payload, Locale.ENGLISH);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        MessageResponse body = assertInstanceOf(MessageResponse.class, response.getBody());
        assertEquals("Allergens updated", body.getMessage());
        assertEquals("", user.getMyAllergens());
        verify(userRepository).save(user);
    }

    @Test
    void updateAvatarPersistsProcessedJpegBytes() {
        AuthController controller = controller();
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);

        User user = new User();
        user.setId(8L);

        when(jwtService.extractUserId(servletRequest)).thenReturn(8L);
        when(userRepository.findByIdAndRemovedAtIsNull(8L)).thenReturn(Optional.of(user));

        byte[] full = new byte[]{1, 2};
        byte[] thumb = new byte[]{3};
        when(avatarImageService.processFromClientPayload("data:image/png;base64,abc123"))
                .thenReturn(new AvatarImageService.ProcessedAvatar(full, thumb));

        UpdateAvatarRequest payload = new UpdateAvatarRequest();
        payload.setAvatarData("data:image/png;base64,abc123");

        ResponseEntity<?> response = controller.updateAvatar(servletRequest, payload, Locale.ENGLISH);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        MessageResponse body = assertInstanceOf(MessageResponse.class, response.getBody());
        assertEquals("Avatar updated", body.getMessage());
        assertArrayEquals(full, user.getAvatarFull());
        assertArrayEquals(thumb, user.getAvatarThumb());
        verify(userRepository).save(user);
    }
}
