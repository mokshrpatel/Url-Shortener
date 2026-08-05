package moksh.urlshortener.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.entity.UrlMapping;
import moksh.urlshortener.repository.UrlMappingRepository;
import moksh.urlshortener.service.UrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALLOWED_CHARACTERS.length();

    private final UrlMappingRepository urlMappingRepository;

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
    public String getOriginalUrl(String shortUrl) {

        Optional<UrlMapping> mapping = urlMappingRepository.findByShortUrl(shortUrl);

        if (mapping.isPresent()) {
            return mapping.get().getLongUrl();
        } else {
            // In a real app, you would throw a custom exception like UrlNotFoundException here
            throw new RuntimeException("URL not found");
        }
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
}
