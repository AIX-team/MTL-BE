package com.example.mytravellink.user.repository;

import com.example.mytravellink.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}
