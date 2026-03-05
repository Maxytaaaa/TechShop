package com.techshop.techshop.controller;

import com.techshop.techshop.config.SecurityConfig;
import com.techshop.techshop.entity.Order;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.security.UserDetailsServiceImpl;
import com.techshop.techshop.service.OAuth2UserService;
import com.techshop.techshop.service.OrderService;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private OAuth2UserService oAuth2UserService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(999.0);
        order.setDeliveryAddress("123 Main St");
        order.setOrderItems(new HashSet<>());
    }

    @Test
    void viewOrders_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "john")
    void viewOrders_Authenticated_ReturnsOrdersView() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        when(orderService.getUserOrders(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @WithMockUser(username = "john")
    void viewOrderDetails_ReturnsOrderDetailsView() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @WithMockUser(username = "john")
    void showCheckoutForm_ReturnsCheckoutView() throws Exception {
        mockMvc.perform(get("/orders/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));
    }

    @Test
    @WithMockUser(username = "john")
    void createOrder_Success_RedirectsToOrderDetails() throws Exception {
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        when(orderService.createOrder(any(), anyString())).thenReturn(order);

        mockMvc.perform(post("/orders/create").with(csrf())
                        .param("deliveryAddress", "123 Main St"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }

    @Test
    @WithMockUser(username = "john")
    void cancelOrder_Success_RedirectsToOrderDetails() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(post("/orders/1/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }
}