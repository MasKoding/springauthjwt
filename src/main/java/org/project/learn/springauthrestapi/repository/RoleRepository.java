package org.project.learn.springauthrestapi.repository;

import org.project.learn.springauthrestapi.entity.ERole;
import org.project.learn.springauthrestapi.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles,Long> {
    Optional<Roles> findByName(ERole name);
}
