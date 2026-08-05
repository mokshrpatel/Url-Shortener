package moksh.urlshortener.controller;

import lombok.RequiredArgsConstructor;
import moksh.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

@Controller // @Controller, because we are returning a View (RedirectView), not JSON
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortUrl}")
    public RedirectView redirect(@PathVariable String shortUrl) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortUrl);

            return new RedirectView(originalUrl);

        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
        }
    }
}
