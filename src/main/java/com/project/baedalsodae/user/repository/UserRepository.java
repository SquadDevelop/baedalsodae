package com.project.baedalsodae.user.repository;

import com.project.baedalsodae.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}
