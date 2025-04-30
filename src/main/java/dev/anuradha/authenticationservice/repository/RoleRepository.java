package dev.anuradha.authenticationservice.repository;

import dev.anuradha.authenticationservice.model.Role;
import dev.anuradha.authenticationservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
