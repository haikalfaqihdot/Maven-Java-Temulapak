package com.temulapak;

import com.temulapak.config.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TemuLapakApplication {

    public static void main(String[] args) {
        // Membuat database PostgreSQL "temulapak" jika belum ada.
        DatabaseInitializer.ensureDatabaseExists();
        SpringApplication.run(TemuLapakApplication.class, args);
    }
}
