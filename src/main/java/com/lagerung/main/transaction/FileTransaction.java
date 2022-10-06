package com.lagerung.main.transaction;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.lagerung.main.interfaces.FilesStorageService;

@Service
public class FileTransaction implements FilesStorageService {

	//Userid Should not contain ~
	final String SEPARATOR = "~";

	private final Path root = Paths.get("uploads");

	// To create Directory where file's will be uploaded temporarily
	@Override
	@PostConstruct
	public void init() {
		try {
			System.out.println("Cleaning up Directory ");
			if (Files.exists(root)) {
				Arrays.stream(root.toFile().listFiles()).forEach(File::delete);
				Files.deleteIfExists(root);
				Files.createDirectory(root);
			} else {

				Files.createDirectory(root);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void save(MultipartFile file, String userid) {

		try {
			System.out.println("save files");
            Files.copy(file.getInputStream(), this.root.resolve(userid + SEPARATOR + file.getOriginalFilename()));
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}
	}

	@Override
	public Resource load(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<Path> loadAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
