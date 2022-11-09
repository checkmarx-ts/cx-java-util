package com.checkmarx.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point into the spring boot application
 * com.checkmarx.sdk is required to autowire the SDK
 */
@SpringBootApplication(scanBasePackages = {"com.checkmarx.sdk", "com.checkmarx.util"})
public class CheckmarxUtilApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CheckmarxUtilApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        System.exit(SpringApplication.exit(app.run(args)));
    }
}
