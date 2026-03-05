package com.techshop.techshop.service;

import com.techshop.techshop.entity.User;
import com.techshop.techshop.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encoded");
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getUserByUsername_Found() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("john");

        assertTrue(result.isPresent());
    }

    @Test
    void getUserByEmail_Found() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("john@test.com");

        assertTrue(result.isPresent());
    }

    @Test
    void existsByUsername_True() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertTrue(userService.existsByUsername("john"));
    }

    @Test
    void existsByEmail_True() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("john@test.com"));
    }

    @Test
    void updateUser_Success() {
        User updated = new User();
        updated.setUsername("john_updated");
        updated.setEmail("new@test.com");
        updated.setFirstName("Johnny");
        updated.setLastName("Doe");
        updated.setPhone("123456789");
        updated.setAddress("123 Main St");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, updated);

        assertEquals("john_updated", result.getUsername());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("Johnny", result.getFirstName());
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                userService.updateUser(99L, user));
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }
}