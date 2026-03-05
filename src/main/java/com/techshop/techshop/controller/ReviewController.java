package com.techshop.techshop.controller;

import com.techshop.techshop.entity.User;
import com.techshop.techshop.service.ReviewService;
import com.techshop.techshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    private User resolveUser(Authentication authentication) {
        String name = authentication.getName();
        return userService.getUserByUsername(name)
                .or(() -> userService.getUserByEmail(name))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/add")
    public String addReview(@RequestParam Long productId,
                            @RequestParam Integer rating,
                            @RequestParam String comment,
                            Authentication authentication) {
        User user = resolveUser(authentication);
        reviewService.createReview(user, productId, rating, comment);
        return "redirect:/products/" + productId;
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                               @RequestParam Long productId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/products/" + productId;
    }
}