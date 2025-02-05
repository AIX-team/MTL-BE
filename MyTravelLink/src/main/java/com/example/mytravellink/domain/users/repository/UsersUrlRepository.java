package com.example.mytravellink.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.users.entity.UsersUrl;
import com.example.mytravellink.domain.users.entity.UsersUrlId;

public interface UsersUrlRepository extends JpaRepository<UsersUrl, UsersUrlId> {

}