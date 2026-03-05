package com.techshop.techshop.controller;

import com.techshop.techshop.entity.Order;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.service.OrderService;
import com.techshop.techshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    private User resolveUser(Authentication authentication) {
        String name = authentication.getName();
        return userService.getUserByUsername(name)
                .or(() -> userService.getUserByEmail(name))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String viewOrders(Authentication authentication, Model model) {
        User user = resolveUser(authentication);
        List<Order> orders = orderService.getUserOrders(user.getId());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id,
                                   Authentication authentication,
                                   Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        return "checkout";
    }

    @PostMapping("/create")
    public String createOrder(@RequestParam String deliveryAddress,
                              Authentication authentication) {
        User user = resolveUser(authentication);
        Order order = orderService.createOrder(user, deliveryAddress);
        return "redirect:/orders/" + order.getId();
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "redirect:/orders/" + id;
    }
}