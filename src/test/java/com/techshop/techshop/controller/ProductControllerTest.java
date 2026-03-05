package com.techshop.techshop.controller;

import com.techshop.techshop.config.SecurityConfig;
import com.techshop.techshop.entity.Category;
import com.techshop.techshop.entity.Product;
import com.techshop.techshop.security.UserDetailsServiceImpl;
import com.techshop.techshop.service.OAuth2UserService;
import com.techshop.techshop.service.CategoryService;
import com.techshop.techshop.service.ReviewService;
import com.techshop.techshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private OAuth2UserService oAuth2UserService;

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
        product.setPrice(999.0);
        product.setStockQuantity(10);
        product.setCategory(category);
    }

    @Test
    void getAllProducts_ReturnsProductsView() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void getProduct_Found_ReturnsDetailView() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(reviewService.getProductReviews(1L)).thenReturn(List.of());
        when(reviewService.getAverageRating(1L)).thenReturn(0.0);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-details"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void getProductsByCategory_ReturnsProductsView() throws Exception {
        when(productService.getProductsByCategory(1L)).thenReturn(List.of(product));
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void searchProducts_ReturnsProductsView() throws Exception {
        when(productService.searchProducts("iphone")).thenReturn(List.of(product));
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/products/search").param("keyword", "iphone"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("keyword", "iphone"));
    }
}