package com.springboot.excelsheet.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private int statusCode;
	private String successMessage;
	private String errorMessage;

}
