package com.techshop.techshop.service;

import com.techshop.techshop.entity.Category;
import com.techshop.techshop.entity.Product;
import com.techshop.techshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setBrand("Apple");
        product.setPrice(999.0);
        product.setStockQuantity(10);
        product.setCategory(category);
    }

    @Test
    void getAllProducts_ReturnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
    }

    @Test
    void getProductById_Found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("iPhone 15", result.get().getName());
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getProductsByCategory_ReturnsList() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        List<Product> result = productService.getProductsByCategory(1L);

        assertEquals(1, result.size());
    }

    @Test
    void searchProducts_ReturnsList() {
        when(productRepository.findByNameContainingIgnoreCase("iphone")).thenReturn(List.of(product));

        List<Product> result = productService.searchProducts("iphone");

        assertEquals(1, result.size());
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
    }

    @Test
    void updateProduct_Success() {
        Product updated = new Product();
        updated.setName("iPhone 16");
        updated.setBrand("Apple");
        updated.setPrice(1099.0);
        updated.setStockQuantity(5);
        updated.setCategory(category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(1L, updated);

        assertEquals("iPhone 16", result.getName());
        assertEquals(1099.0, result.getPrice());
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                productService.updateProduct(99L, product));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).deleteById(1L);
    }

    @Test
    void isProductInStock_True() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertTrue(productService.isProductInStock(1L, 5));
    }

    @Test
    void isProductInStock_False() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertFalse(productService.isProductInStock(1L, 15));
    }

    @Test
    void reduceStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.reduceStock(1L, 3);

        assertEquals(7, product.getStockQuantity());
    }

    @Test
    void reduceStock_NotEnoughStock_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () ->
                productService.reduceStock(1L, 20));
    }
}