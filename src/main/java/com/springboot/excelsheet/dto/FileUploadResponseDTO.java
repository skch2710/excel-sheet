package com.springboot.excelsheet.dto;

import lombok.Data;

@Data
public class FileUploadResponseDTO {

	private String successMessage;
	private String failureMessage;
	private String failFilePath;

}
