package com.example.mytravellink.domain.url.service;

import com.example.mytravellink.api.url.dto.UrlRequest;
import com.example.mytravellink.api.url.dto.UrlResponse;


public interface UrlService {

  UrlResponse processUrl(UrlRequest request);
}
