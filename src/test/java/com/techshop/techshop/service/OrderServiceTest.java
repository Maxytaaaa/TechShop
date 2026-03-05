package com.techshop.techshop.service;

import com.techshop.techshop.entity.*;
import com.techshop.techshop.repository.BasketRepository;
import com.techshop.techshop.repository.OrderItemRepository;
import com.techshop.techshop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BasketRepository basketRepository;
    @Mock
    private BasketService basketService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Basket basket;
    private BasketItem basketItem;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");

        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(999.0);
        product.setStockQuantity(10);

        basket = new Basket();
        basket.setId(1L);
        basket.setUser(user);

        basketItem = new BasketItem();
        basketItem.setId(1L);
        basketItem.setProduct(product);
        basketItem.setQuantity(2);
        basketItem.setBasket(basket);

        Set<BasketItem> items = new HashSet<>();
        items.add(basketItem);
        basket.setItems(items);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(1998.0);
        order.setDeliveryAddress("123 Main St");
        order.setOrderItems(new HashSet<>());
    }

    @Test
    void createOrder_Success() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(productService.isProductInStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(basketService).clearBasket(user);

        Order result = orderService.createOrder(user, "123 Main St");

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals("123 Main St", result.getDeliveryAddress());
        verify(basketService).clearBasket(user);
    }

    @Test
    void createOrder_BasketNotFound_ThrowsException() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(user, "123 Main St"));
    }

    @Test
    void createOrder_EmptyBasket_ThrowsException() {
        basket.setItems(new HashSet<>());
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(user, "123 Main St"));
    }

    @Test
    void createOrder_ProductOutOfStock_ThrowsException() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(productService.isProductInStock(1L, 2)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(user, "123 Main St"));
    }

    @Test
    void getUserOrders_ReturnsList() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getUserOrders(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.getOrderById(99L));
    }

    @Test
    void getAllOrders_ReturnsList() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
    }

    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateOrderStatus(1L, "SHIPPED");

        assertEquals("SHIPPED", result.getStatus());
    }

    @Test
    void updateOrderStatus_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.updateOrderStatus(99L, "SHIPPED"));
    }

    @Test
    void cancelOrder_Success() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        order.getOrderItems().add(orderItem);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));
        assertEquals("CANCELLED", order.getStatus());
        assertEquals(12, product.getStockQuantity());
    }

    @Test
    void cancelOrder_NotPending_ThrowsException() {
        order.setStatus("SHIPPED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () ->
                orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.cancelOrder(99L));
    }
}