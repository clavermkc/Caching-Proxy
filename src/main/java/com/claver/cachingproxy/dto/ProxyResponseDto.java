package com.claver.cachingproxy.dto;

import org.springframework.http.HttpHeaders;

// Utilisation d'un record pour représenter la réponse du proxy
public record ProxyResponseDto(
        String body,
        HttpHeaders headers,
        String cacheStatus // Contiendra "HIT" ou "MISS"
) {}