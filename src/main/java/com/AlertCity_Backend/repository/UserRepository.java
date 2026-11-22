package com.AlertCity_Backend.repository;

import com.AlertCity_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUser(String user);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserOrEmail(String user, String email);

    boolean existsByUser(String user);

    boolean existsByEmail(String email);
}
