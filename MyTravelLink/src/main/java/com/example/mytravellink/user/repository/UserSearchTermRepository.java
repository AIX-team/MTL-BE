package com.example.mytravellink.user.repository;

import com.example.mytravellink.user.domain.UserSearchTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSearchTermRepository extends JpaRepository<UserSearchTerm, String> {

}

