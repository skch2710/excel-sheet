package com.springboot.excelsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
	
	private String studentId;
	private String fullName;
	private String emailId;
	private String mobileNumber;
	private String course;
	private String fee;
	
}
