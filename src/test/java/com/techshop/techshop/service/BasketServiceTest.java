package com.techshop.techshop.service;

import com.techshop.techshop.entity.*;
import com.techshop.techshop.repository.BasketItemRepository;
import com.techshop.techshop.repository.BasketRepository;
import com.techshop.techshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketRepository basketRepository;
    @Mock
    private BasketItemRepository basketItemRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BasketService basketService;

    private User user;
    private Product product;
    private Basket basket;
    private BasketItem basketItem;

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
        basket.setItems(new HashSet<>());

        basketItem = new BasketItem();
        basketItem.setId(1L);
        basketItem.setBasket(basket);
        basketItem.setProduct(product);
        basketItem.setQuantity(2);
    }

    @Test
    void getOrCreateBasket_Exists() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));

        Basket result = basketService.getOrCreateBasket(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(basketRepository, never()).save(any());
    }

    @Test
    void getOrCreateBasket_Creates() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(basketRepository.save(any())).thenReturn(basket);

        Basket result = basketService.getOrCreateBasket(user);

        assertNotNull(result);
        verify(basketRepository).save(any());
    }

    @Test
    void addProductToBasket_NewItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasketIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(basketItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BasketItem result = basketService.addProductToBasket(user, 1L, 2);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void addProductToBasket_ExistingItem_IncreasesQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasketIdAndProductId(1L, 1L)).thenReturn(Optional.of(basketItem));
        when(basketItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BasketItem result = basketService.addProductToBasket(user, 1L, 3);

        assertEquals(5, result.getQuantity());
    }

    @Test
    void addProductToBasket_NotEnoughStock_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () ->
                basketService.addProductToBasket(user, 1L, 20));
    }

    @Test
    void addProductToBasket_ProductNotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                basketService.addProductToBasket(user, 99L, 1));
    }

    @Test
    void removeProductFromBasket_Success() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasketIdAndProductId(1L, 1L)).thenReturn(Optional.of(basketItem));
        doNothing().when(basketItemRepository).delete(basketItem);

        assertDoesNotThrow(() -> basketService.removeProductFromBasket(user, 1L));
        verify(basketItemRepository).delete(basketItem);
    }

    @Test
    void removeProductFromBasket_BasketNotFound_ThrowsException() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                basketService.removeProductFromBasket(user, 1L));
    }

    @Test
    void updateQuantity_Success() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasketIdAndProductId(1L, 1L)).thenReturn(Optional.of(basketItem));
        when(basketItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> basketService.updateQuantity(user, 1L, 5));
        assertEquals(5, basketItem.getQuantity());
    }

    @Test
    void updateQuantity_NotEnoughStock_ThrowsException() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        when(basketItemRepository.findByBasketIdAndProductId(1L, 1L)).thenReturn(Optional.of(basketItem));

        assertThrows(RuntimeException.class, () ->
                basketService.updateQuantity(user, 1L, 99));
    }

    @Test
    void clearBasket_Success() {
        basket.getItems().add(basketItem);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));
        doNothing().when(basketItemRepository).deleteAll(any());
        when(basketRepository.save(any())).thenReturn(basket);

        assertDoesNotThrow(() -> basketService.clearBasket(user));
        verify(basketItemRepository).deleteAll(any());
    }

    @Test
    void calculateTotal_Success() {
        basket.getItems().add(basketItem);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));

        Double total = basketService.calculateTotal(user);

        assertEquals(1998.0, total);
    }

    @Test
    void calculateTotal_EmptyBasket() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basket));

        Double total = basketService.calculateTotal(user);

        assertEquals(0.0, total);
    }
}