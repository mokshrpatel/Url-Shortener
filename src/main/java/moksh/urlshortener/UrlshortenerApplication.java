package moksh.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlshortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlshortenerApplication.class, args);
        System.out.println("Hello world");
    }

}
