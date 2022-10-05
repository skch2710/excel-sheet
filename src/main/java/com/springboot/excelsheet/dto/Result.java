package com.springboot.excelsheet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result {

	private int statusCode;
	private String successMessage;
	private String errorMessage;
	private Object data;

	public Result(Object data) {
		this.data = data;
	}
}
