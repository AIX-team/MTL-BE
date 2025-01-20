package com.example.mytravellink.guidebook.repository;

import com.mytravellink.guidebook.domain.entity.GuideBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repositroy.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.sterotype.Repository;

@Repository
public interface GuideBookRepository extends JpaRepository<GuideBook, Long>{
    // repository 작성

}
