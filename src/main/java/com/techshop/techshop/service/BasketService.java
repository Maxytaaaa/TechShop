package com.techshop.techshop.service;

import com.techshop.techshop.entity.Basket;
import com.techshop.techshop.entity.BasketItem;
import com.techshop.techshop.entity.Product;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.repository.BasketRepository;
import com.techshop.techshop.repository.BasketItemRepository;
import com.techshop.techshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasketService {

    private final BasketRepository basketRepository;
    private final BasketItemRepository basketItemRepository;
    private final ProductRepository productRepository;

    public Basket getOrCreateBasket(User user) {
        Optional<Basket> existingBasket = basketRepository.findByUserId(user.getId());

        if (existingBasket.isPresent()) {
            return existingBasket.get();
        }

        Basket newBasket = new Basket();
        newBasket.setUser(user);
        newBasket.setItems(new HashSet<>());
        return basketRepository.save(newBasket);
    }

    public BasketItem addProductToBasket(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        Basket basket = getOrCreateBasket(user);

        Optional<BasketItem> existingItem = basketItemRepository
                .findByBasketIdAndProductId(basket.getId(), productId);

        if (existingItem.isPresent()) {
            BasketItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return basketItemRepository.save(item);
        } else {
            BasketItem newItem = new BasketItem();
            newItem.setBasket(basket);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return basketItemRepository.save(newItem);
        }
    }

    public void removeProductFromBasket(User user, Long productId) {
        Basket basket = basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        BasketItem item = basketItemRepository.findByBasketIdAndProductId(basket.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product not in basket"));

        basketItemRepository.delete(item);
    }

    public void updateQuantity(User user, Long productId, Integer quantity) {
        Basket basket = basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        BasketItem item = basketItemRepository.findByBasketIdAndProductId(basket.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product not in basket"));

        Product product = item.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        item.setQuantity(quantity);
        basketItemRepository.save(item);
    }

    public void clearBasket(User user) {
        Basket basket = basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        basketItemRepository.deleteAll(basket.getItems());
        basket.getItems().clear();
        basketRepository.save(basket);
    }

    public Double calculateTotal(User user) {
        Basket basket = basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        return basket.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
}