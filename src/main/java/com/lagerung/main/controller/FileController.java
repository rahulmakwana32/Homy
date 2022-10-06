package com.lagerung.main.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lagerung.main.transaction.FileTransaction;

@RestController
public class FileController {

	@Autowired
	FileTransaction filetransaction;

	@PostMapping("/upload")
	public String uploadFiles(@RequestParam("files") MultipartFile[] files,@RequestParam String userid) {
		String message = "";
		try {
			System.out.println("Uploading File");
			
			//Define the list for filenames
			List<String> fileNames = new ArrayList<>();

			
			//Iterate through the MultipartFile files  and invoke Save method of FileTransaction
			Arrays.asList(files).stream().forEach(file -> {
				filetransaction.save(file, userid);
				fileNames.add(file.getOriginalFilename());
				System.out.println(fileNames);
			});

			message = "Uploaded the files successfully: " + fileNames;
			return message;
		} catch (Exception e) {
			message = "Fail to upload files!";
			return "";
		}
	}

	@GetMapping("/download")
	public String downloadfiles() {

		return "";

	}

}
