package com.example.mytravellink.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.users.domain.UsersUrl;
import com.example.mytravellink.users.domain.UsersUrlId;

public interface UsersUrlRepository extends JpaRepository<UsersUrl, UsersUrlId> {

}