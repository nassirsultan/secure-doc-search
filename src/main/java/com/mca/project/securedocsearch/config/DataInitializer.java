package com.mca.project.securedocsearch.config;

import com.mca.project.securedocsearch.model.Role;
import com.mca.project.securedocsearch.model.User;
import com.mca.project.securedocsearch.repository.RoleRepository;
import com.mca.project.securedocsearch.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // create roles if not exist
        Role adminRole = roleRepo.findByRoleName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role(); r.setRoleName("ROLE_ADMIN"); return roleRepo.save(r);
        });
        Role userRole = roleRepo.findByRoleName("ROLE_USER").orElseGet(() -> {
            Role r = new Role(); r.setRoleName("ROLE_USER"); return roleRepo.save(r);
        });

        // create admin user if not exists
        if (!userRepo.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin@123")); // change later
            admin.setRoles(Set.of(adminRole, userRole));
            userRepo.save(admin);
            System.out.println("Created default admin user -> username: admin, password: admin@123");
        }
    }
}
