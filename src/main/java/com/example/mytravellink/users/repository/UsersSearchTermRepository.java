package com.example.mytravellink.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.users.domain.UsersSearchTerm;

public interface UsersSearchTermRepository extends JpaRepository<UsersSearchTerm, String> {

}

