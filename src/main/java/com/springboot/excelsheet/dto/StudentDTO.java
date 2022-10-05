package com.springboot.excelsheet.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
	
	private Long studentId;
	private String fullName;
	private String emailId;
	private Long mobileNumber;
	private String course;
	private BigDecimal fee;
	
}
