package com.springboot.excelsheet.service;

import org.springframework.web.multipart.MultipartFile;

import com.springboot.excelsheet.dto.Result;
import com.springboot.excelsheet.dto.StudentDTO;

public interface StudentService {

	Result save(StudentDTO studentDTO);

	String getAllStudents();

	Result uploadExcelSheet(MultipartFile file);
	
	Result findAll();
}
