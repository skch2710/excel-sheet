package com.springboot.excelsheet.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.excelsheet.dto.Result;
import com.springboot.excelsheet.exception.CustomException;
import com.springboot.excelsheet.service.StudentService;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

	@Autowired
	private StudentService studentService;

	/**
	 * Upload file.
	 *
	 * @param file the file
	 * @return the response entity
	 */
	@PostMapping(path = "/upload", consumes = { "application/json", "multipart/form-data" })
	public ResponseEntity<Result> uploadExcelSheet(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(studentService.uploadExcelSheet(file));
	}

	/**
	 * Download Error file.
	 *
	 * @param filePath the file path
	 * @param request  the request
	 * @return the response entity
	 */
	@GetMapping("/downloadErrorFile")
	public ResponseEntity<Resource> downloadFile(@RequestParam String filePath, HttpServletRequest request) {
		// Load file as Resource
		Resource resource;
		Path path = Paths.get(filePath);
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.println("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);

	}

	/**
	 * Download Students file.
	 *
	 * @param request the request
	 * @return the response entity
	 */
	@GetMapping("/downloadStudents")
	public ResponseEntity<Resource> downloadStudents(HttpServletRequest request) {
		String filePath = studentService.getAllStudents();
		Resource resource;
		String contentType = "application/octet-stream";
		String headerValue = null;
		try {
			Path path = Paths.get(filePath);
			resource = new UrlResource(path.toUri());
			headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, headerValue).body(resource);
	}

	@GetMapping("/findAll")
	public ResponseEntity<Result> findAll() {
		return ResponseEntity.ok(studentService.findAll());
	}

}
