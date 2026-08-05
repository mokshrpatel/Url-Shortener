package moksh.urlshortener.service;

public interface UrlService {
    String shortenUrl(String originalUrl);
    String getOriginalUrl(String shortUrl);
}