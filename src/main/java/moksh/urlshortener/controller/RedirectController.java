package moksh.urlshortener.controller;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@Controller // @Controller, because we are returning a View (RedirectView), not JSON
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{shortUrl}")
    public RedirectView redirect(@PathVariable String shortUrl, HttpServletRequest request) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortUrl);

            // Asynchronously send click event to Kafka
            Map<String, String> eventData = new HashMap<>();
            eventData.put("shortUrl", shortUrl);
            eventData.put("ipAddress", request.getRemoteAddr());
            eventData.put("userAgent", request.getHeader("User-Agent"));
            
            try {
                String message = objectMapper.writeValueAsString(eventData);
                kafkaTemplate.send("url-clicks", message);
            } catch (Exception e) {
                System.err.println("Failed to send Kafka message: " + e.getMessage());
            }

            return new RedirectView(originalUrl);

        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
        }
    }
}
