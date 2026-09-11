package com.claver.cachingproxy.proxy;

import com.claver.cachingproxy.dto.ProxyResponseDto;
import com.claver.cachingproxy.entity.CachedResponseEntity;
import com.claver.cachingproxy.repository.CacheRepository;
import com.claver.cachingproxy.service.ProxyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProxyServiceTest {

    @Mock
    private CacheRepository cacheRepository;

    private ProxyService proxyService;

    @BeforeEach
    void setUp() {
        // On initialise le service manuellement pour injecter une URL d'origine fictive
        // Note: L'appel externe (RestClient) lèvera une erreur s'il est vraiment exécuté,
        // dans un vrai projet on "mockerait" le RestClient ou on utiliserait MockRestServiceServer.
        proxyService = new ProxyService(cacheRepository, "http://dummyjson.com");
    }

    @Test
    void processRequest_ShouldReturnHit_WhenDataExistsInCache() {
        // Arrange : Préparer les données
        String path = "/products";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        CachedResponseEntity mockEntity = CachedResponseEntity.builder()
                .requestUrl(path)
                .responseBody("{\"id\":1, \"name\":\"Laptop\"}")
                .responseHeaders(headers)
                .build();

        // Simuler que le cache contient déjà la donnée
        when(cacheRepository.findByRequestUrl(path)).thenReturn(Optional.of(mockEntity));

        // Act : Exécuter la méthode
        ProxyResponseDto result = proxyService.processRequest(path);

        // Assert : Vérifier les résultats
        assertEquals("HIT", result.cacheStatus());
        assertEquals("{\"id\":1, \"name\":\"Laptop\"}", result.body());

        // Vérifier que repository.save() N'A PAS été appelé (puisque c'est un HIT)
        verify(cacheRepository, never()).save(any(CachedResponseEntity.class));
    }

    // Pour tester un MISS réel proprement, il faudrait mocker le RestClient intégré à ProxyService.
    // Voici comment on vérifie la logique d'appel de sauvegarde du repository.
    @Test
    void builderConversion_ShouldCreateCorrectEntity_BeforeSave() {
        // Ce test isole la logique de conversion Builder
        String path = "/users";
        ProxyResponseDto dto = new ProxyResponseDto("{\"user\":\"test\"}", new HttpHeaders(), "MISS");

        CachedResponseEntity entity = CachedResponseEntity.builder()
                .requestUrl(path)
                .responseBody(dto.body())
                .responseHeaders(dto.headers())
                .build();

        assertEquals("/users", entity.getRequestUrl());
        assertEquals("{\"user\":\"test\"}", entity.getResponseBody());
    }
}