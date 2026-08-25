package moksh.urlshortener.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.entity.User;
import moksh.urlshortener.entity.UrlMapping;
import moksh.urlshortener.repository.UrlMappingRepository;
import moksh.urlshortener.repository.UserRepository;
import moksh.urlshortener.repository.ClickAnalyticsRepository;
import moksh.urlshortener.service.UrlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALLOWED_CHARACTERS.length();

    private final UrlMappingRepository urlMappingRepository;
    private final UserRepository userRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;

    @Override
    @Transactional
    public String shortenUrl(String originalUrl) {
        // 1. Check if we already shortened this URL to prevent duplicates
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByLongUrl(originalUrl);
        if (existingMapping.isPresent()) {
            return existingMapping.get().getShortUrl();
        }

        UrlMapping newMapping = new UrlMapping("", originalUrl);
        newMapping = urlMappingRepository.save(newMapping);

        String shortUrl = encodeBase62(newMapping.getId());

        newMapping.setShortUrl(shortUrl);
        urlMappingRepository.save(newMapping);

        return shortUrl;
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#shortUrl")
    public String getOriginalUrl(String shortUrl) {
        System.out.println("CACHE MISS: Fetching long URL for '" + shortUrl + "' from PostgreSQL Database!");

        Optional<UrlMapping> mapping = urlMappingRepository.findByShortUrl(shortUrl);

        if (mapping.isPresent()) {
            return mapping.get().getLongUrl();
        } else {
            // In a real app, you would throw a custom exception like UrlNotFoundException here
            throw new RuntimeException("URL not found");
        }
    }

    @Override
    @Transactional
    public String createCustomAlias(String originalUrl, String customAlias, String userEmail) {
        if (customAlias == null || !customAlias.matches("^[a-zA-Z0-9]+$")) {
            throw new RuntimeException("Custom alias must be alphanumeric only");
        }
        if (customAlias.length() > 10) {
            throw new RuntimeException("Custom alias cannot exceed 10 characters");
        }

        // 1. Ensure user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if original URL already has an alias
        Optional<UrlMapping> existingMappingByUrl = urlMappingRepository.findByLongUrl(originalUrl);
        if (existingMappingByUrl.isPresent()) {
            throw new RuntimeException("This URL already has an alias: " + existingMappingByUrl.get().getShortUrl());
        }

        // 3. Check if custom alias is already taken
        Optional<UrlMapping> existingMappingByAlias = urlMappingRepository.findByShortUrl(customAlias);
        if (existingMappingByAlias.isPresent()) {
            throw new RuntimeException("This custom alias is already taken");
        }

        // 4. Create and save new mapping
        long customId = decodeBase62(customAlias);
        urlMappingRepository.insertCustomAlias(customId, customAlias, originalUrl, user.getId(), java.time.LocalDateTime.now());
        
        return customAlias;
    }


    private String encodeBase62(long id) {
        if (id == 0) {
            return String.valueOf(ALLOWED_CHARACTERS.charAt(0));
        }

        StringBuilder encodedString = new StringBuilder();
        while (id > 0) {
            encodedString.append(ALLOWED_CHARACTERS.charAt((int) (id % BASE)));
            id = id / BASE;
        }

        // The characters are generated in reverse order, so we must reverse the string
        return encodedString.reverse().toString();
    }

    private long decodeBase62(String shortUrl) {
        long id = 0;
        for (int i = 0; i < shortUrl.length(); i++) {
            id = id * BASE + ALLOWED_CHARACTERS.indexOf(shortUrl.charAt(i));
        }
        return id;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getAllAnalytics(String userEmail) {
        java.util.List<UrlMapping> userUrls = urlMappingRepository.findByUser_Email(userEmail);
        java.util.List<java.util.Map<String, Object>> analyticsList = new java.util.ArrayList<>();
        
        for (UrlMapping mapping : userUrls) {
            long count = clickAnalyticsRepository.countByShortUrl(mapping.getShortUrl());
            java.util.Map<String, Object> stat = new java.util.HashMap<>();
            stat.put("shortUrl", mapping.getShortUrl());
            stat.put("longUrl", mapping.getLongUrl());
            stat.put("totalClicks", count);
            analyticsList.add(stat);
        }
        
        return analyticsList;
    }
}
