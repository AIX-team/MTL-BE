package com.example.mytravellink.url.repository.query;

import com.example.mytravellink.url.domain.Url;

import java.util.List;

public interface UrlQueryRepository {
  public List<Url> findAllByEmailId(String emailId);
}
