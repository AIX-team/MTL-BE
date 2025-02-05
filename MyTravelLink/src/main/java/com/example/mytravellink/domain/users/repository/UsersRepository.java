package com.example.mytravellink.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.users.entity.Users;

public interface UsersRepository extends JpaRepository<Users, String> {

  Optional<Users> findByEmail(String email);

}
