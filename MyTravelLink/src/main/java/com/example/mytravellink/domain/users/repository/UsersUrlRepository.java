package com.example.mytravellink.domain.users.repository;

import com.example.mytravellink.api.user.dto.LinkDataResponse;
import com.example.mytravellink.domain.users.entity.UsersUrl;
import com.example.mytravellink.domain.users.entity.UsersUrlId;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UsersUrlRepository extends CrudRepository<UsersUrl, UsersUrlId> {

    @Query("SELECT new com.example.mytravellink.api.user.dto.LinkDataResponse(u.id, u.urlTitle, u.url, u.updateAt) " +
           "FROM UsersUrl uu JOIN uu.url u " +
           "WHERE uu.user.email = :email AND uu.isUse = true " +
           "ORDER BY u.updateAt DESC")
    List<LinkDataResponse> findTopActiveLinks(@Param("email") String email, Pageable pageable);
}