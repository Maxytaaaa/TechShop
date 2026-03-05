package com.techshop.techshop.controller;

import com.techshop.techshop.config.SecurityConfig;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.security.UserDetailsServiceImpl;
import com.techshop.techshop.service.OAuth2UserService;
import com.techshop.techshop.service.AuthService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private OAuth2UserService oAuth2UserService;

    @Test
    void showLoginForm_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void showRegisterForm_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void registerUser_Success_RedirectsToLogin() throws Exception {
        when(authService.registerUser(any(), any(), any(), any(), any())).thenReturn(new User());

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "john")
                        .param("email", "john@test.com")
                        .param("password", "password123")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void registerUser_UsernameExists_ReturnsRegisterWithError() throws Exception {
        when(authService.registerUser(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "john")
                        .param("email", "john@test.com")
                        .param("password", "password123")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }
}