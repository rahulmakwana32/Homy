package com.spring.hcloud.main.service;

import java.io.*;
import java.net.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jni.Thread;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.spring.hcloud.main.model.MetaData;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

@Service
public class Fileoperationservice {

	private final Path root = Paths.get("uploads");

	String SAVE = "save";
	String FETCH = "fetch";
	String SAVEFORDOWNLOAD = "savefordownload";

	public void init() {
		try {
			// Files.delete(root);
			if (!Files.exists(root)) {
				Files.createDirectory(root);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	public String save(MultipartFile file, SseEmitter sseEmitter, String username) {

		Function<String, Boolean> storefile = s -> new Fileoperationservice().StoreFile(s, file, sseEmitter, username);
		Function<Boolean, String> notifyclient = s -> new Fileoperationservice().notifyclient(SAVE, s, file, sseEmitter,
				username);

		return storefile.andThen(notifyclient)
				.apply("Start Processing file" + file.getOriginalFilename() + " for user " + username);

	}

	public Boolean savefordownload(MultipartFile file, SseEmitter sseEmitter, String username) {

		Function<String, Boolean> storefile = s -> new Fileoperationservice().StoreFile(s, file, sseEmitter, username);
		/*
		 * Function<Boolean, String> notifyclient = s -> new
		 * Fileoperationservice().notifyclient(SAVEFORDOWNLOAD, s, file, sseEmitter,
		 * username);
		 */

		return storefile.apply("Start Processing file" + file.getOriginalFilename() + " for user " + username);

	}

	public String fetchfilelist(SseEmitter sseEmitter, String username) {
		Function<Boolean, String> notifyclient = s -> new Fileoperationservice().notifyclient("FETCH", s, null,
				sseEmitter, username);

		return notifyclient.apply(true);

	}

	public Boolean StoreFile(String s, MultipartFile file, SseEmitter sseEmitter, String username) {
		try {
			System.out.println(s);
			Files.copy(file.getInputStream(), this.root.resolve(username + "_" + file.getOriginalFilename()),
					StandardCopyOption.REPLACE_EXISTING);
			System.out.println("File Stored Successfully locally");
			return true;
		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("File Storage failed");
			return false;
		}
	}

	public String notifyclient(String message, Boolean s, MultipartFile file, SseEmitter sseEmitter, String username) {
		if (s && message.equals("save")) {
			try {

				System.out.println("Informing client about file arrival " + file.getContentType() + "-" + file.getSize()
						+ " " + file.getContentType());
				sseEmitter.send(file.getOriginalFilename(), MediaType.APPLICATION_XML);

				return "File " + file.getOriginalFilename()
						+ " have uploaded Successfully,Sent event to client for Sync up";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "File " + file.getOriginalFilename()
						+ " have uploaded Successfully.However,There some issue informing the end client.File's will sync up once it get reconnected";

			}

		} else if (s && message.equals("fetch")) {

			try {
				System.out.println("Informing client about fetch request arrival");
				sseEmitter.send("fetchfiles", MediaType.APPLICATION_XML);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return "Notification to client machine have failed.Try requesting again.";
			}

			return "File Upload have failed.Please Re-upload the file";

		} else {

			return "File Upload have failed.Please Re-upload the file";
		}

	}

	public String notifyclientpendingfile(String filename, SseEmitter sseEmitter, String username) {

		try {
			System.out.println("Informing notifyclientpendingfile about file arrival--->" + filename.substring(5));
			sseEmitter.send(filename.substring(filename.indexOf("_") + 1));

			return "File " + filename + " have uploaded Successfully,Sent event to client for Sync up";

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "File " + filename
					+ " have uploaded Successfully.However,There some issue informing the end client.File's will sync up once it get reconnected";

		}

	}

	public Resource loadFile(String username, String fileName) {

		System.out.println("Downloading File ");
		try {

			Path filePath = this.root.resolve(username + "_" + fileName).normalize();

			System.out.println("loadFileeeee " + filePath.toUri());
			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists()) {
				return resource;
			} else {
				System.out.println("File not found  ");
				throw new FileNotFoundException("File not found " + fileName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public void deletefile(SseEmitter sseEmitter, String username, String fileName) throws IOException {
		System.out.println("deleting " + fileName);
		sseEmitter.send("User Request to deletefile=" + fileName);

	}

	public void notifyenduser(SseEmitter sseEmitter, String username, Boolean isAuth) {

		System.out.println("notifyenduser for fetching reqiets");
		if (isAuth) {

			try {
				sseEmitter.send("Request for Fetchfiles from user:" + username);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void downloadZipFile(HttpServletResponse response, List<String> listOfFileNames) {
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=download.zip");
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			for (String fileName : listOfFileNames) {
				System.out.println("Adding Zip file " + fileName);
				FileSystemResource fileSystemResource = new FileSystemResource(fileName);
				ZipEntry zipEntry = new ZipEntry(fileSystemResource.getFilename());
				zipEntry.setSize(fileSystemResource.contentLength());
				zipEntry.setTime(System.currentTimeMillis());

				zipOutputStream.putNextEntry(zipEntry);

				StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
				zipOutputStream.closeEntry();
			}
			zipOutputStream.finish();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public List<String> getFilesClient(String username) {
		// Creates an array in which we will store the names of files and directories
		String[] pathnames;
		List<String> getFiles = new ArrayList<String>();

		// Creates a new File instance by converting the given pathname string
		// into an abstract pathname
		File f = new File(this.root.toUri());

		// Populates the array with names of files and directories
		pathnames = f.list();

		// For each pathname in the pathnames array
		for (String pathname : pathnames) {
			// Print the names of files and directories
			System.out.println("getfile " + pathname);
			if (pathname.startsWith(username + "_")) {
				getFiles.add(f.getPath() + "/" + pathname);

			}

		}
		System.out.println("getfile " + getFiles.size());

		return getFiles;

	}

	public void InvokeDao(MultipartFile[] file, String username) {
		System.out.println("Invoking Dao");
		Date sysdate = new Date();

		List<MetaData> files = new ArrayList<MetaData>();
		Arrays.asList(file).forEach(m -> {

			files.add(new MetaData(m.getOriginalFilename(), m.getContentType(), username, "PENDING", (int) m.getSize(),
					sysdate, null, true, true));

		});

		String responseJson = WebClient.builder().build().post().uri("http://localhost:8080/api/savefilemetadata")
				.syncBody(files).headers(httpHeaders -> {
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				}).retrieve().bodyToMono(String.class).block();

		System.out.println("Invoking Dao" + responseJson);
		// client.build()
		// .post().uri("http://localhost:8080/api/savefilemetadata").
		// bodyValue( );

	}

	public void deletefile(String username, String filename) {
		try {
		File file = new File(this.root.resolve(username+"_"+filename).toUri());
		file.delete();
 		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
