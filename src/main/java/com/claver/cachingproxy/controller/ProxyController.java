package com.claver.cachingproxy.controller;

import com.claver.cachingproxy.dto.ProxyResponseDto;
import com.claver.cachingproxy.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ProxyController {

    private final ProxyService proxyService;
    private final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping("/**")
    public ResponseEntity<String> handleProxyRequest(HttpServletRequest request) {
        // Reconstruire le chemin complet (ex: /products?limit=10)
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = (queryString == null) ? path : path + "?" + queryString;

        // Déléguer au service
        ProxyResponseDto proxyResponse = proxyService.processRequest(fullPath);

        // Préparer les en-têtes de la réponse
        HttpHeaders responseHeaders = new HttpHeaders();
        if (proxyResponse.headers() != null) {
            responseHeaders.putAll(proxyResponse.headers());
        }

        // Ajouter l'en-tête X-Cache demandé par les spécifications de roadmap.sh
        responseHeaders.set("X-Cache", proxyResponse.cacheStatus());
        logger.info("X-Cache:{}",proxyResponse.cacheStatus());
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(proxyResponse.body());
    }
    @PostMapping("/internal/clear-cache")
    public ResponseEntity<String> clearCache() {
        proxyService.clear(); // Assure-toi d'avoir une méthode clear() dans ton repo
        return ResponseEntity.ok("Cache cleared successfully");
    }
}