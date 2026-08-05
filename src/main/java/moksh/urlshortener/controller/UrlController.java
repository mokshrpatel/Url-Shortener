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
}