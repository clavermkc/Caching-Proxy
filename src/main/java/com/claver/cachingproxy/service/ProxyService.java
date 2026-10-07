package com.claver.cachingproxy.service;

import com.claver.cachingproxy.dto.ProxyResponseDto;
import com.claver.cachingproxy.entity.CachedResponseEntity;
import com.claver.cachingproxy.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class ProxyService {
    private final CacheRepository cacheRepository;
    private final RestClient restClient;
    private final String originUrl;

    public ProxyService(CacheRepository cacheRepository,
                        @Value("${proxy.origin:https://dummyjson.com}") String originUrl) {
        this.cacheRepository = cacheRepository;
        this.originUrl = (originUrl != null) ? originUrl.trim().replace("\"", "").replaceAll("/+$", "") : "";
        this.restClient = RestClient.create();
    }

    public ProxyResponseDto processRequest(String pathAndQuery) {
        Optional<CachedResponseEntity> cachedEntityOpt = cacheRepository.findByRequestUrl(pathAndQuery);

        // --- CAS 1 : CACHE HIT ---
        if (cachedEntityOpt.isPresent()) {
            CachedResponseEntity entity = cachedEntityOpt.get();
            return new ProxyResponseDto(
                    entity.getResponseBody(),
                    entity.getResponseHeaders(),
                    "HIT"
            );
        }

        // --- CAS 2 : CACHE MISS ---
        String normalizedPath = (pathAndQuery != null && pathAndQuery.startsWith("/")) ? pathAndQuery : "/" + (pathAndQuery != null ? pathAndQuery : "");
        String targetUrl = originUrl + normalizedPath;

        // Appel au serveur d'origine
        ResponseEntity<String> originResponse = restClient.get()
                .uri(targetUrl)
                .retrieve()
                .toEntity(String.class);

        // Création du DTO avec la réponse de l'origine

        ProxyResponseDto responseDto = new ProxyResponseDto(
                originResponse.getBody(),
                originResponse.getHeaders(),
                "MISS"
        );
        assert originResponse.getBody() != null;


        // Conversion du DTO en Entity via le Builder pour le sauvegarder
        CachedResponseEntity newEntity = CachedResponseEntity.builder()
                .requestUrl(pathAndQuery)
                .responseBody(responseDto.body())
                .responseHeaders(responseDto.headers())
                .build();

        cacheRepository.save(newEntity);
        return responseDto;
    }

    public void clear() {
        cacheRepository.clear();
    }
}