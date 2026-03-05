package com.techshop.techshop.config;

import com.techshop.techshop.entity.Role;
import com.techshop.techshop.repository.RoleRepository;
import com.techshop.techshop.repository.UserRepository;
import com.techshop.techshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        System.out.println("Roles initialized: USER, ADMIN");

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            System.out.println("Admin bootstrap disabled (APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD not set).");
            return;
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            authService.registerAdmin(
                    "admin",
                    adminEmail,
                    adminPassword,
                    "Maksym",
                    "Leno"
            );
            System.out.println("Admin user created: admin");
        }
    }
}