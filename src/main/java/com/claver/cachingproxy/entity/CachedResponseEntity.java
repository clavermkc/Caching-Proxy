package com.claver.cachingproxy.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
@Builder
public class CachedResponseEntity {
    private String requestUrl; // Identifiant unique (ex: /products)
    private String responseBody;
    private HttpHeaders responseHeaders;
}