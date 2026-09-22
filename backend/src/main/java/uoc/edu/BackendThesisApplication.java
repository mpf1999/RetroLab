package uoc.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BackendThesisApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendThesisApplication.class, args);
    }
}
