package com.techshop.techshop.controller;

import com.techshop.techshop.config.SecurityConfig;
import com.techshop.techshop.entity.Category;
import com.techshop.techshop.entity.Order;
import com.techshop.techshop.entity.Product;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.security.UserDetailsServiceImpl;
import com.techshop.techshop.service.OAuth2UserService;
import com.techshop.techshop.service.CategoryService;
import com.techshop.techshop.service.OrderService;
import com.techshop.techshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private OAuth2UserService oAuth2UserService;

    private Product product;
    private Category category;
    private Order order;

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

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(999.0);
        order.setOrderItems(new HashSet<>());
    }

    @Test
    void adminDashboard_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void adminDashboard_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminDashboard_AsAdmin_ReturnsDashboardView() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("productsCount"))
                .andExpect(model().attributeExists("ordersCount"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void manageProducts_AsAdmin_ReturnsProductsView() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showCreateProductForm_ReturnsProductForm() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProduct_Success_RedirectsToProducts() throws Exception {
        when(productService.createProduct(any())).thenReturn(product);

        mockMvc.perform(post("/admin/products").with(csrf())
                        .param("name", "iPhone 15")
                        .param("price", "999.0")
                        .param("stockQuantity", "10")
                        .param("brand", "Apple"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showEditProductForm_ReturnsProductForm() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateProduct_Success_RedirectsToProducts() throws Exception {
        when(productService.updateProduct(anyLong(), any())).thenReturn(product);

        mockMvc.perform(post("/admin/products/edit/1").with(csrf())
                        .param("name", "iPhone 16")
                        .param("price", "1099.0")
                        .param("stockQuantity", "5")
                        .param("brand", "Apple"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteProduct_Success_RedirectsToProducts() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(post("/admin/products/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void manageOrders_AsAdmin_ReturnsOrdersView() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateOrderStatus_Success_RedirectsToOrders() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), anyString())).thenReturn(order);

        mockMvc.perform(post("/admin/orders/1/status").with(csrf())
                        .param("status", "SHIPPED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void manageCategories_AsAdmin_ReturnsCategoriesView() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showCreateCategoryForm_ReturnsCategoryForm() throws Exception {
        mockMvc.perform(get("/admin/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-form"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_Success_RedirectsToCategories() throws Exception {
        when(categoryService.createCategory(any())).thenReturn(category);

        mockMvc.perform(post("/admin/categories").with(csrf())
                        .param("name", "Electronics")
                        .param("description", "Electronic devices"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showEditCategoryForm_ReturnsCategoryForm() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/admin/categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-form"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateCategory_Success_RedirectsToCategories() throws Exception {
        when(categoryService.updateCategory(anyLong(), any())).thenReturn(category);

        mockMvc.perform(post("/admin/categories/edit/1").with(csrf())
                        .param("name", "Smartphones")
                        .param("description", "Mobile phones"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_Success_RedirectsToCategories() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(post("/admin/categories/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }
}