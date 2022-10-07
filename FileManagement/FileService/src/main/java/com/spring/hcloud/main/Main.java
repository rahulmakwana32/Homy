package com.spring.hcloud.main;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.spring.hcloud.main.service.Fileoperationservice;
import com.spring.hcloud.main.service.Globalservice;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@Component
@EnableSwagger2
@EnableScheduling
public class Main implements CommandLineRunner {

	private final Path root = Paths.get("uploads");

	

	
	@Autowired
	Fileoperationservice fileoperationservice;

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	public void run(String... args) throws Exception {

		System.out.println("running apps");
		fileoperationservice.init();
	}

	@Autowired
	Globalservice sseEmitters;

	// Scan Folder for file and inform client
	@Scheduled(fixedDelay = 5000000L
			)
	public void scanandsend() {

		String[] pathnames;
		System.out.println("Scanning"+new java.util.Date());
		pathnames = new File(root.toString()).list();

		for (String file : pathnames) {
			String username = file.substring(0, file.indexOf("_"));
			System.out.println("founf files for "+ username);
			// Print the names of files and directories

			SseEmitter sseEmitter = sseEmitters.get(username);
			if (sseEmitter != null  ) {
				System.out.println("founf files for "+ username);
				fileoperationservice.notifyclientpendingfile(file, sseEmitter, username);
			}
		}

	}

}
