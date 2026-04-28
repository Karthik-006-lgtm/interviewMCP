package com.interviewprep.platform.config;

import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.entity.enums.UserRole;
import com.interviewprep.platform.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    public ApplicationRunner bootstrapAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String adminEmail,
            @Value("${app.bootstrap-admin.password:}") String adminPassword,
            @Value("${app.bootstrap-admin.full-name:Platform Admin}") String adminFullName
    ) {
        return args -> {
            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                return;
            }

            String normalizedEmail = adminEmail.trim().toLowerCase();
            User admin = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(User::new);
            boolean created = admin.getId() == null;

            admin.setFullName(StringUtils.hasText(admin.getFullName()) ? admin.getFullName() : adminFullName.trim());
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));

            Set<UserRole> roles = new LinkedHashSet<>(admin.getRoles());
            roles.add(UserRole.USER);
            roles.add(UserRole.ADMIN);
            admin.setRoles(roles);

            userRepository.save(admin);
            log.info("{} bootstrap admin account for {}", created ? "Created" : "Updated", normalizedEmail);
        };
    }
}
