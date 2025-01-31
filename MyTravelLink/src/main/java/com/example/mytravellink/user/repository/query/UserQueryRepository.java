package com.example.mytravellink.user.repository.query;



import com.example.mytravellink.user.domain.User;

import java.util.List;

public interface UserQueryRepository {
  public List<User> findByDelete(Boolean delete);
}
