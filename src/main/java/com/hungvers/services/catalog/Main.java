package com.hungvers.services.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
