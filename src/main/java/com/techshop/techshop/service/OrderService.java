package com.techshop.techshop.service;

import com.techshop.techshop.entity.*;
import com.techshop.techshop.repository.BasketRepository;
import com.techshop.techshop.repository.OrderRepository;
import com.techshop.techshop.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BasketRepository basketRepository;
    private final BasketService basketService;
    private final ProductService productService;

    @Transactional
    public Order createOrder(User user, String deliveryAddress) {
        return createOrder(user, deliveryAddress, "CASH");
    }

    @Transactional
    public Order createOrder(User user, String deliveryAddress, String paymentMethod) {
        Basket basket = basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        if (basket.getItems().isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);

        Set<OrderItem> orderItems = new HashSet<>();
        double totalPrice = 0.0;

        for (BasketItem basketItem : basket.getItems()) {
            Product product = basketItem.getProduct();

            if (!productService.isProductInStock(product.getId(), basketItem.getQuantity())) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(basketItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);
            totalPrice += product.getPrice() * basketItem.getQuantity();

            productService.reduceStock(product.getId(), basketItem.getQuantity());
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        basketService.clearBasket(user);

        return savedOrder;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals("PENDING")) {
            throw new RuntimeException("Can only cancel pending orders");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals("CANCELLED")) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }

        orderRepository.deleteById(id);
    }
}