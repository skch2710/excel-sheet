package com.springboot.excelsheet.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.excelsheet.dao.StudentDAO;
import com.springboot.excelsheet.dto.FileUploadResponseDTO;
import com.springboot.excelsheet.dto.Result;
import com.springboot.excelsheet.dto.StudentDTO;
import com.springboot.excelsheet.dto.UploadStdentDTO;
import com.springboot.excelsheet.exception.CustomException;
import com.springboot.excelsheet.model.Student;
import com.springboot.excelsheet.service.StudentService;
import com.springboot.excelsheet.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

	@Autowired
	private StudentDAO studentDAO;

	/** The servlet context. */
	@Autowired
	ServletContext servletContext;

	/** The file path. */
	private static String filePath = null;

	/**
	 * Initialize.
	 */
	@PostConstruct
	private void initialize() {
		filePath = servletContext.getRealPath("/upload/");
	}

	@Override
	public Result save(StudentDTO studentDTO) {
		return null;
	}

	@Override
	public Result getAllStudents() {
		return null;
	}

	@Override
	public Result uploadExcelSheet(MultipartFile file) {
		Result result = null;
		try {

			List<String> errorList = ExcelHelper.validateTemplate(file.getInputStream());

			if (errorList.size() > 0) {
				result = new Result(errorList);
				result.setStatusCode(HttpStatus.BAD_REQUEST.value());
				result.setErrorMessage("Upload template headers are not matched");
			} else {
				List<UploadStdentDTO> uploadErrorStudentDataList = new ArrayList<>();
				List<UploadStdentDTO> userList = ExcelHelper.excelToStudentData(file);

				for (UploadStdentDTO uploadStdentDTO : userList) {
					Student student = studentDAO.findByEmailId(uploadStdentDTO.getEmailId());
					if (student != null) {
						if (!"".equals(uploadStdentDTO.getFailureReason())) {
							uploadStdentDTO.setFailureReason(
									uploadStdentDTO.getFailureReason() + ", \"Email ID\" already exists");
						} else {
							uploadStdentDTO.setFailureReason("\"Email ID\" already exists");
						}
					}
					if ("".equals(uploadStdentDTO.getFailureReason())) {

						Student student2 = new Student();

						student2.setFullName(uploadStdentDTO.getFullName());
						student2.setEmailId(uploadStdentDTO.getEmailId());
						student2.setMobileNumber(Long.parseLong(uploadStdentDTO.getMobileNumber()));
						student2.setCourse(uploadStdentDTO.getCourse());
						student2.setFee(new BigDecimal(uploadStdentDTO.getFee()));

						studentDAO.save(student2);
					} else {
						uploadErrorStudentDataList.add(uploadStdentDTO);
					}
				}
				FileUploadResponseDTO fileUploadResponseDTO = new FileUploadResponseDTO();
				if (uploadErrorStudentDataList.size() > 0) {
					fileUploadResponseDTO.setFailureMessage(uploadErrorStudentDataList.size() + " Error(s) Found");
					String path = errorFileGeneration(uploadErrorStudentDataList, file.getOriginalFilename());
					fileUploadResponseDTO.setFailFilePath(path);
				}
				if (userList.size() > 0) {
					fileUploadResponseDTO.setSuccessMessage(
							userList.size() - uploadErrorStudentDataList.size() + " Record(s) Uploaded");
				}
				result = new Result(fileUploadResponseDTO);
				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("Successfully uploaded");
			}

		} catch (Exception e) {
			log.error("error in upload", e);
			throw new CustomException("fail to store excel data: ", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	/**
	 * Error file generation.
	 *
	 * @param uploadUserDataDTOs the upload user data DT os
	 * @param originalFileName   the original file name
	 * @return the string
	 */
	private static String errorFileGeneration(List<UploadStdentDTO> uploadStudentDataDTOs, String originalFileName) {
		XSSFWorkbook workbook = null;
		String errorPath = "";
		try {
			workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet();
			XSSFRow row;
			int index = 1;
			Map<Integer, Object[]> uploadErrorData = new TreeMap<Integer, Object[]>();
			uploadErrorData.put(index++,
					new Object[] { "Full Name", "Email ID", "Mobile Number", "Course", "Fee", "Failure Reason" });

			for (UploadStdentDTO uploadStudentDataDTO : uploadStudentDataDTOs) {
				uploadErrorData.put(index++,
						new Object[] { uploadStudentDataDTO.getFullName(), uploadStudentDataDTO.getEmailId(),
								uploadStudentDataDTO.getMobileNumber(), uploadStudentDataDTO.getCourse(),
								uploadStudentDataDTO.getFee(), uploadStudentDataDTO.getFailureReason() });

			}

			Set<Integer> keys = uploadErrorData.keySet();

			int rowid = 0;
			XSSFCellStyle style = workbook.createCellStyle();

			style.setFillForegroundColor(IndexedColors.RED.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			// writing the data into the sheets...
			for (Integer key : keys) {

				row = spreadsheet.createRow(rowid++);
				Object[] objects = uploadErrorData.get(key);
				int cellid = 0;

				for (Object obj : objects) {
					Cell cell = row.createCell(cellid++);
					cell.setCellValue((String) obj);
//	                cell.setCellStyle(style);
				}
			}

			String actualPath = filePath + "error" + File.separator + new Date().getTime();
			String errorFullFilePath = actualPath + File.separator + "Error_" + originalFileName;
			boolean exists = new File(actualPath).exists();
			if (!exists) {
				new File(actualPath).mkdirs();
			}
			// writing the workbook into the file...
			FileOutputStream out = new FileOutputStream(new File(errorFullFilePath));

			workbook.write(out);
			out.close();
			errorPath = errorFullFilePath;
		} catch (Exception e) {
			log.error("error in errorFileGeneration", e);
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					log.error("error in errorFileGeneration while workbook closing", e);
				}
			}
		}
		return errorPath;
	}

}
