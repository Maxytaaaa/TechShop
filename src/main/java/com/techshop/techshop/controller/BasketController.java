package com.techshop.techshop.controller;

import com.techshop.techshop.entity.Basket;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.service.BasketService;
import com.techshop.techshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/basket")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;
    private final UserService userService;

    private User resolveUser(Authentication authentication) {
        String name = authentication.getName();
        return userService.getUserByUsername(name)
                .or(() -> userService.getUserByEmail(name))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String viewBasket(Authentication authentication, Model model) {
        User user = resolveUser(authentication);
        Basket basket = basketService.getOrCreateBasket(user);
        Double total = basketService.calculateTotal(user);
        model.addAttribute("basket", basket);
        model.addAttribute("total", total);
        return "basket";
    }

    @PostMapping("/add")
    public String addToBasket(@RequestParam Long productId,
                              @RequestParam Integer quantity,
                              Authentication authentication) {
        User user = resolveUser(authentication);
        basketService.addProductToBasket(user, productId, quantity);
        return "redirect:/basket";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromBasket(@PathVariable Long productId,
                                   Authentication authentication) {
        User user = resolveUser(authentication);
        basketService.removeProductFromBasket(user, productId);
        return "redirect:/basket";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam Integer quantity,
                                 Authentication authentication) {
        User user = resolveUser(authentication);
        basketService.updateQuantity(user, productId, quantity);
        return "redirect:/basket";
    }

    @PostMapping("/clear")
    public String clearBasket(Authentication authentication) {
        User user = resolveUser(authentication);
        basketService.clearBasket(user);
        return "redirect:/basket";
    }
}