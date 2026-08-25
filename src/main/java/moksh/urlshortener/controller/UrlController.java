package moksh.urlshortener.controller;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        String longUrl = request.get("longUrl");

        if (longUrl == null || longUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("The 'longUrl' field is required.");
        }

        try {
            String shortUrl = urlService.shortenUrl(longUrl);
            // In a real app, actual domain name should be here
            String fullShortUrl = "http://localhost:8080/" + shortUrl;

            return ResponseEntity.ok(Map.of(
                    "originalUrl", longUrl,
                    "shortUrl", fullShortUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while shortening the URL.");
        }
    }

    @PostMapping("/custom")
    public ResponseEntity<?> createCustomAlias(
            @RequestBody Map<String, String> request,
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken authentication
    ) {
        String longUrl = request.get("longUrl");
        String customAlias = request.get("customAlias");

        if (longUrl == null || longUrl.trim().isEmpty() || customAlias == null || customAlias.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Both 'longUrl' and 'customAlias' fields are required.");
        }

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to create a custom alias.");
        }

        String email = authentication.getPrincipal().getAttribute("email");

        try {
            String shortUrl = urlService.createCustomAlias(longUrl, customAlias, email);
            String fullShortUrl = "http://localhost:8080/" + shortUrl;

            return ResponseEntity.ok(Map.of(
                    "originalUrl", longUrl,
                    "shortUrl", fullShortUrl,
                    "isCustom", true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating custom alias.");
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to view analytics.");
        }
        
        String email = authentication.getPrincipal().getAttribute("email");
        
        try {
            java.util.List<Map<String, Object>> analytics = urlService.getAllAnalytics(email);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}