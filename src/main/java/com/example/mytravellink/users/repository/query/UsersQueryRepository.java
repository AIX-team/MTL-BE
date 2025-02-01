package com.example.mytravellink.users.repository.query;

import java.util.List;

import com.example.mytravellink.users.domain.Users;

public interface UsersQueryRepository {
  public List<Users> findByDelete(Boolean delete);
}
