package com.techshop.techshop.controller;

import com.techshop.techshop.config.SecurityConfig;
import com.techshop.techshop.entity.Basket;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.security.UserDetailsServiceImpl;
import com.techshop.techshop.service.OAuth2UserService;
import com.techshop.techshop.service.BasketService;
import com.techshop.techshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasketController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BasketService basketService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private OAuth2UserService oAuth2UserService;

    private User user;
    private Basket basket;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");

        basket = new Basket();
        basket.setId(1L);
        basket.setUser(user);
        basket.setItems(new HashSet<>());
    }

    @Test
    void viewBasket_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/basket"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "john")
    void viewBasket_Authenticated_ReturnsBasketView() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        when(basketService.getOrCreateBasket(user)).thenReturn(basket);
        when(basketService.calculateTotal(user)).thenReturn(0.0);

        mockMvc.perform(get("/basket"))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attributeExists("basket"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @WithMockUser(username = "john")
    void addToBasket_Success_RedirectsToBasket() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        when(basketService.addProductToBasket(any(), anyLong(), anyInt())).thenReturn(null);

        mockMvc.perform(post("/basket/add").with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));
    }

    @Test
    @WithMockUser(username = "john")
    void removeFromBasket_Success_RedirectsToBasket() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        doNothing().when(basketService).removeProductFromBasket(any(), anyLong());

        mockMvc.perform(post("/basket/remove/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));
    }

    @Test
    @WithMockUser(username = "john")
    void updateQuantity_Success_RedirectsToBasket() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        doNothing().when(basketService).updateQuantity(any(), anyLong(), anyInt());

        mockMvc.perform(post("/basket/update").with(csrf())
                        .param("productId", "1")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));
    }

    @Test
    @WithMockUser(username = "john")
    void clearBasket_Success_RedirectsToBasket() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        doNothing().when(basketService).clearBasket(any());

        mockMvc.perform(post("/basket/clear").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/basket"));
    }
}