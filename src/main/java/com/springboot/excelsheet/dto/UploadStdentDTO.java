package com.springboot.excelsheet.dto;

import lombok.Data;

@Data
public class UploadStdentDTO {

	private String fullName;
	private String emailId;
	private String mobileNumber;
	private String course;
	private String fee;
	private String failureReason;

}
