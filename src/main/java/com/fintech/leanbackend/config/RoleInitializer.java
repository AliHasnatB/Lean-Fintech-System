package com.fintech.leanbackend.config;

import com.fintech.leanbackend.entites.Role;
import com.fintech.leanbackend.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@Component
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    @Override
    @Transactional
    public void run(String... args) {
        List<String> defaultRoles = List.of("ADMIN", "USER");

        defaultRoles.forEach(roleName -> {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created new role: {}", roleName);
            } else {
                log.debug("Role already exists: {}", roleName);
            }
        });
    }
}
