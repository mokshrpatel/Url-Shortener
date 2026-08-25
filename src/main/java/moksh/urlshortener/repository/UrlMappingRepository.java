package moksh.urlshortener.repository;

import moksh.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortUrl(String shortUrl);

    Optional<UrlMapping> findByLongUrl(String longUrl);
    
    java.util.List<UrlMapping> findByUser_Email(String email);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "INSERT INTO url_mapping (id, short_url, long_url, user_id, created_at) VALUES (:id, :shortUrl, :longUrl, :userId, :createdAt)", nativeQuery = true)
    void insertCustomAlias(@org.springframework.data.repository.query.Param("id") Long id, 
                           @org.springframework.data.repository.query.Param("shortUrl") String shortUrl, 
                           @org.springframework.data.repository.query.Param("longUrl") String longUrl, 
                           @org.springframework.data.repository.query.Param("userId") Long userId, 
                           @org.springframework.data.repository.query.Param("createdAt") java.time.LocalDateTime createdAt);
}
