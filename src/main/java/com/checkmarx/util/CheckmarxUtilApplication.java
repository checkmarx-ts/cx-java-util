package com.checkmarx.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.checkmarx.sdk","com.checkmarx.util"})
public class CheckmarxUtilApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CheckmarxUtilApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		SpringApplication.exit(app.run(args));
	}
}
