package com.example.mytravellink.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.users.entity.UsersSearchTerm;

public interface UsersSearchTermRepository extends JpaRepository<UsersSearchTerm, String> {

}

