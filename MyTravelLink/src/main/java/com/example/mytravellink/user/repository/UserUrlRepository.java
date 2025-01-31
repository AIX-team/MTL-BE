package com.example.mytravellink.user.repository;

import com.example.mytravellink.user.domain.UserUrl;
import com.example.mytravellink.user.domain.UserUrlId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUrlRepository extends JpaRepository<UserUrl, UserUrlId> {

}