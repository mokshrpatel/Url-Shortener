package moksh.urlshortener.repository;

import moksh.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {
    List<ClickAnalytics> findByShortUrl(String shortUrl);
    long countByShortUrl(String shortUrl);
}
