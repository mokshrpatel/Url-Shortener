package moksh.urlshortener.service;

public interface UrlService {
    String shortenUrl(String originalUrl);
    String getOriginalUrl(String shortUrl);
    String createCustomAlias(String originalUrl, String customAlias, String userEmail);
    java.util.List<java.util.Map<String, Object>> getAllAnalytics(String userEmail);
}