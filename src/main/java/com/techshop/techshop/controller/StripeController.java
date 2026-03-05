package com.techshop.techshop.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.service.BasketService;
import com.techshop.techshop.service.OrderService;
import com.techshop.techshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final UserService userService;
    private final BasketService basketService;
    private final OrderService orderService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private User resolveUser(Authentication authentication) {
        String name = authentication.getName();
        return userService.getUserByUsername(name)
                .or(() -> userService.getUserByEmail(name))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/create-checkout-session")
    public String createCheckoutSession(@RequestParam String deliveryAddress,
                                        Authentication authentication,
                                        HttpSession session) throws Exception {
        User user = resolveUser(authentication);
        Double total = basketService.calculateTotal(user);

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/stripe/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/orders/checkout")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((long) (total * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("TechShop Order")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session stripeSession = Session.create(params);

        session.setAttribute("pendingDeliveryAddress", deliveryAddress);
        session.setAttribute("pendingUserId", user.getId());

        return "redirect:" + stripeSession.getUrl();
    }

    @GetMapping("/success")
    public String handleSuccess(@RequestParam("session_id") String sessionId,
                                Authentication authentication,
                                HttpSession session) {
        User user = resolveUser(authentication);
        String deliveryAddress = (String) session.getAttribute("pendingDeliveryAddress");

        if (deliveryAddress == null) {
            return "redirect:/basket";
        }

        com.techshop.techshop.entity.Order order = orderService.createOrder(user, deliveryAddress, "CARD");
        session.removeAttribute("pendingDeliveryAddress");
        session.removeAttribute("pendingUserId");

        return "redirect:/orders/" + order.getId();
    }
}