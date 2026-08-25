package moksh.urlshortener.service;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.entity.ClickAnalytics;
import moksh.urlshortener.repository.ClickAnalyticsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "url-clicks", groupId = "url-analytics-group")
    public void consumeClickEvent(String message) {
        try {
            Map<String, String> eventData = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            
            ClickAnalytics analytics = ClickAnalytics.builder()
                    .shortUrl(eventData.get("shortUrl"))
                    .ipAddress(eventData.get("ipAddress"))
                    .userAgent(eventData.get("userAgent"))
                    .build();
            
            clickAnalyticsRepository.save(analytics);
            
            System.out.println("Analytics saved for: " + analytics.getShortUrl());
        } catch (Exception e) {
            System.err.println("Error processing Kafka message: " + e.getMessage());
        }
    }
}
