package com.springboot.excelsheet.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> customeExceptionHandler(CustomException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse();

		errorResponse.setStatusCode(ex.getStatus().value());
		errorResponse.setSuccessMessage(ex.getStatus().name());
		errorResponse.setErrorMessage(ex.getMessage());

		return new ResponseEntity<>(errorResponse, ex.getStatus());
	}

}
