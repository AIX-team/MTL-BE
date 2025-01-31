package com.example.mytravellink.user.repository.query;

import com.example.mytravellink.user.domain.QUser;
import com.example.mytravellink.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {
  
  private final JPAQueryFactory queryFactory;
  private static final QUser user = new QUser("user");

  @Override
  public List<User> findByDelete(Boolean delete) {
    return queryFactory.selectFrom(user)
      .where(user.isDelete.eq(delete))
      .fetch();
  }
}
