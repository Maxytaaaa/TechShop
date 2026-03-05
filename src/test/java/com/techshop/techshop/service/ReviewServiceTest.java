package com.techshop.techshop.service;

import com.techshop.techshop.entity.Product;
import com.techshop.techshop.entity.Review;
import com.techshop.techshop.entity.User;
import com.techshop.techshop.repository.ProductRepository;
import com.techshop.techshop.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");

        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");

        review = new Review();
        review.setId(1L);
        review.setUser(user);
        review.setProduct(product);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReview_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Review result = reviewService.createReview(user, 1L, 5, "Great product!");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great product!", result.getComment());
    }

    @Test
    void createReview_ProductNotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                reviewService.createReview(user, 99L, 5, "Great!"));
    }

    @Test
    void createReview_RatingTooLow_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () ->
                reviewService.createReview(user, 1L, 0, "Bad"));
    }

    @Test
    void createReview_RatingTooHigh_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () ->
                reviewService.createReview(user, 1L, 6, "Too good"));
    }

    @Test
    void getProductReviews_ReturnsList() {
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review));

        List<Review> result = reviewService.getProductReviews(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getUserReviews_ReturnsList() {
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(review));

        List<Review> result = reviewService.getUserReviews(1L);

        assertEquals(1, result.size());
    }

    @Test
    void deleteReview_Success() {
        doNothing().when(reviewRepository).deleteById(1L);

        assertDoesNotThrow(() -> reviewService.deleteReview(1L));
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void getAverageRating_Success() {
        Review review2 = new Review();
        review2.setRating(3);
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review, review2));

        Double avg = reviewService.getAverageRating(1L);

        assertEquals(4.0, avg);
    }

    @Test
    void getAverageRating_NoReviews_ReturnsZero() {
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of());

        Double avg = reviewService.getAverageRating(1L);

        assertEquals(0.0, avg);
    }
}