package com.example.mytravellink.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.users.entity.UsersUrl;
import com.example.mytravellink.domain.users.entity.UsersUrlId;
import com.example.mytravellink.api.user.dto.LinkDataResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsersUrlRepository extends JpaRepository<UsersUrl, UsersUrlId> {

    @Query("SELECT new com.example.mytravellink.api.user.dto.LinkDataResponse(u.id, u.urlTitle, uu.updateAt) " +
           "FROM UsersUrl uu JOIN Url u ON uu.id.urlId = u.id " +
           "WHERE uu.id.email = :email AND uu.isUse = true " +
           "ORDER BY uu.updateAt DESC")
    List<LinkDataResponse> findTopActiveLinks(@Param("email") String email, Pageable pageable);
}