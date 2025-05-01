package dev.anuradha.authenticationservice.config;

import dev.anuradha.authenticationservice.model.Role;
import dev.anuradha.authenticationservice.model.RoleName;
import dev.anuradha.authenticationservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.findByName(RoleName.USER) == null) {
            roleRepository.save(new Role(null, RoleName.USER));
        }
        if (roleRepository.findByName(RoleName.ADMIN) == null) {
            roleRepository.save(new Role(null, RoleName.ADMIN));
        }
    }
}
