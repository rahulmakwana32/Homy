package com.spring.hcloud.main.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.spring.hcloud.main.service.Fileoperationservice;
import com.spring.hcloud.main.service.Globalservice;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://10.0.0.146:3000" })

public class ApiController {

//UploadFiles	

	@Autowired
	Fileoperationservice fileoperationservice;

	@GetMapping("/")
	public String getHome() {

		/*
		 * return "<!DOCTYPE html>\n" + "<html>\n" + "   <head>\n" +
		 * "      <title>Upload multiple files</title>\n" + "   </head>\n" + "\n" +
		 * "   <body>\n" +
		 * "<form action=\"http://localhost:8383/uploadfiles\" method=\"POST\" enctype=\"multipart/form-data\">\n"
		 * + "      \n" +
		 * "       Username : <input type=\"text\" id=\"username\" name=\"username\"><br><br>\n"
		 * + "         <input type=\"file\" name=\"file\" multiple><br><br>\n" +
		 * "         After uploading multiple files, click Submit.<br>\n" +
		 * "         \n" + "         <input type=\"submit\" value=\"Submit\">\n" +
		 * "      </form>\n" + "   </body>\n" + "</html>";
		 */

		return "<!DOCTYPE html>\n" + "<html>\n" + "   <head>\n" + "      <title>Upload multiple files</title>\n"
				+ "   </head>\n" + "\n" + "   <body>\n"
				+ "<form action=\"http://3.88.84.68:8383/uploadfiles\" method=\"POST\" enctype=\"multipart/form-data\">\n"
				+ "      \n" + "       Username : <input type=\"text\" id=\"username\" name=\"username\"><br><br>\n"
				+ "         <input type=\"file\" name=\"file\" multiple><br><br>\n"
				+ "         After uploading multiple files, click Submit.<br>\n" + "         \n"
				+ "         <input type=\"submit\" value=\"Submit\">\n" + "      </form>\n" + "   </body>\n"
				+ "</html>";
	}

	// private Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
	@Autowired
	Globalservice sseEmitters;

	@GetMapping("/subscribe/{username}")
	public SseEmitter eventEmitter(@PathVariable("username") String username) throws Exception {
		// Create SSEEmitter Object
		SseEmitter sseEmitter = new SseEmitter(3600000L);
		// Store Created object with username key in Map

		sseEmitters.putCache(username, sseEmitter);

		// sseEmitter.send(username + " Connected at " + new Date().toLocaleString());
		// sseEmitter.onCompletion(() -> sseEmitters.remove(username));
		sseEmitter.onTimeout(() -> {
			System.out.println("Timeout, Removing the SSEEmitter " + username);
			sseEmitters.removeCache(username);
		});
		// System.out.println("SseEmitter Object created with " + username);
		return sseEmitter;
	}

	@PostMapping(path = "/uploadfiles")
	public String uploadfiles(@RequestParam("file") MultipartFile[] files, @RequestParam("username") String username)
			throws IOException {

		fileoperationservice.InvokeDao(files, username);

		return Arrays.asList(files).stream().map(file -> {

			return fileoperationservice.save(file, sseEmitters.get(username), username);

		}).collect(Collectors.toList()).toString();

	}

	@PostMapping(path = "/uploadfiles/{myuser}")
	public String uploadfilesfromdevice(@RequestParam("file") MultipartFile[] files,
			@PathVariable("myuser") String username) throws IOException {

		System.out.println("uploadfilesfromdevice");
		return Arrays.asList(files).stream().map(file -> {

			return fileoperationservice.savefordownload(file, sseEmitters.get(username), username);

		}).collect(Collectors.toList()).toString();

	}

//FetchFile

	@GetMapping(path = "/getfiles/{username}/{filename}")
	public ResponseEntity<Resource> getfiles(@PathVariable("username") String username,
			@PathVariable("filename") String filename) {

		System.out.println("Request for file download");
		Resource resource = fileoperationservice.loadFile(username, filename);

		if (resource == null) {

			System.out.println("NOOOOOOOOOOO");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);

	}

	@GetMapping(path = "/ui/getfiles/{username}/{filename}")
	public ResponseEntity<Resource> getfilesfromui(@PathVariable("username") String username,
			@PathVariable("filename") String filename) {

		System.out.println("getfilesfromui");
		// Get the Object
		SseEmitter sseEmitter = sseEmitters.get(username);

		// Authorise request
		Boolean isAuth = true;

		// Inform the client about request of fetch of all files.
		fileoperationservice.notifyenduser(sseEmitter, username, isAuth);
		try {
			Thread.sleep(10000L);
			System.out.println("Notofied user and waited for 10 seconds.Check folder");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Request made for file download " + filename);
		Resource resource = fileoperationservice.loadFile(username, filename);

		if (resource == null) {

			System.out.println("NOOOOOOOOOOO");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);

	}

//FetchAllFiles
	@GetMapping(path = "/getfiles/{username}")
	public void fetchfiles(@PathVariable("username") String username, HttpServletResponse response) {

		// Get the Object
		SseEmitter sseEmitter = sseEmitters.get(username);

		// Authorise request
		Boolean isAuth = true;

		// Inform the client about request of fetch of all files.
		fileoperationservice.notifyenduser(sseEmitter, username, isAuth);
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> listOfFileNames = fileoperationservice.getFilesClient(username);

		System.out.println(listOfFileNames.size());
		fileoperationservice.downloadZipFile(response, listOfFileNames);

	}

//deleteFiles

	@GetMapping(path = "/ui/deletefiles/{username}/{filename}")
	public String deletefiles(@PathVariable("username") String username, @PathVariable("filename") String filename) {

		System.out.println("Deleting " + filename);
		SseEmitter sseEmitter = sseEmitters.get(username);

		try {
			fileoperationservice.deletefile(sseEmitter, username, filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Deleted Successfully";

	}

	@DeleteMapping(path = "/deletefiles/{username}/{filename}")
	public String deletefilesfromdevice(@PathVariable("username") String username,
			@PathVariable("filename") String filename) {

		System.out.println("Deleting " + filename);
		// SseEmitter sseEmitter = sseEmitters.get(username);

		try {
			fileoperationservice.deletefile(username, filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Deleted Successfully";

	}

}
