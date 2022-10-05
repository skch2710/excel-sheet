package com.springboot.excelsheet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.excelsheet.dto.UploadStdentDTO;
import com.springboot.excelsheet.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExcelHelper {

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
		System.out.println(filePath);
	}

	/** The type. */
	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	/** The HEADERS. */
	static String[] HEADERs = { "Full Name", "Email ID", "Mobile Number", "Course", "Fee" };

	/** The sheet. */
	static String SHEET = "Student_Template";

	/**
	 * Checks for excel format.
	 *
	 * @param file the file
	 * @return true, if successful
	 */
	public static boolean hasExcelFormat(MultipartFile file) {
		if (!TYPE.equals(file.getContentType())) {
			return false;
		}
		return true;
	}

	/**
	 * Validate template.
	 *
	 * @param InputStream the is
	 * @return the list
	 */
	public static List<String> validateTemplate(InputStream is) {
		List<String> list = new ArrayList<String>();
		Workbook workbook = null;
		try {

			String error = "";
			workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheetAt(0);
			Row headerRow = sheet.getRow(0);

			for (int i = 0; i < HEADERs.length; i++) {
				Cell cell = headerRow.getCell(i);
				if (cell == null) {
					error = "Missing \"".concat(HEADERs[i]).concat("\"");
					list.add(error);
				} else if (cell != null && !HEADERs[i].equalsIgnoreCase(cell.getStringCellValue())) {
					error = "\"" + cell.getStringCellValue().concat("\" is not appropriate. Acceptable value is \"")
							.concat(HEADERs[i]).concat("\"");
					list.add(error);
				}
			}
			if (HEADERs.length < headerRow.getPhysicalNumberOfCells()) {
				for (int i = HEADERs.length; i < headerRow.getPhysicalNumberOfCells(); i++) {
					Cell cell = headerRow.getCell(i);
					error = "\"" + cell.getStringCellValue().concat("\" not required");
					list.add(error);
				}
			}

		} catch (Exception e) {
			log.error("error in validateTemplate", e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				log.error("error in validateTemplate ", e);
				throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return list;
	}
	
	/**
	 * Excel to Student data.
	 *
	 * @param file the file
	 * @param fileName the file name
	 * @return the list
	 */
	public static List<UploadStdentDTO> excelToStudentData(MultipartFile file) {
		List<UploadStdentDTO> studentDataList = new ArrayList<>();
		Workbook workbook = null;
		InputStream inputStream = null;

		try {
			String actualPath = filePath + new Date().getTime();
			String originalFullFilePath = actualPath + File.separator + file.getOriginalFilename();
			boolean exists = new File(actualPath).exists();
			if (!exists) {
				new File(actualPath).mkdirs();
			}
			byte[] bytes = file.getBytes();
			FileOutputStream fos = new FileOutputStream(originalFullFilePath);
			fos.write(bytes);
			fos.flush();
			fos.close();

			inputStream = new FileInputStream(new File(actualPath + File.separator + file.getOriginalFilename()));
			workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();
				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				UploadStdentDTO stdentDTO = new UploadStdentDTO();
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < HEADERs.length; i++) {
					Cell cell = currentRow.getCell(i);
					switch (i) {
					case 0:
						if (cell != null && !cell.getStringCellValue().equals("")) {
							stdentDTO.setFullName(cell.getStringCellValue());
							if (!isAlphaNumeric(cell.getStringCellValue())) {
								stringBuilder.append("\"").append(HEADERs[i]
										+ "\" is invalid. Acceptable response must be an alphanumeric value");
							}
						} else {
							stringBuilder.append("\"").append(HEADERs[i] + "\" must be entered");
						}
						break;
					case 1:
						if (cell != null && !cell.getStringCellValue().equals("")) {
							stdentDTO.setEmailId(cell.getStringCellValue());
							if (!isEmailValid(cell.getStringCellValue())) {
								stringBuilder.append("\"")
										.append(HEADERs[i] + "\" is invalid. Acceptable response must be valid email");
							}
						} else {
							if (stringBuilder.length() > 0) {
								stringBuilder.append(",\"").append(HEADERs[i] + "\" must be entered");
							}
						}
						break;

					case 2:
						if (cell != null && cell.getCellType() == CellType.NUMERIC) {
							stdentDTO.setMobileNumber(NUMBER_FORMAT("0", cell.getNumericCellValue()));
						} else if (cell != null) {
							stdentDTO.setMobileNumber(cell.getStringCellValue());
							if (stringBuilder.length() > 0) {
								stringBuilder.append(",\"").append(HEADERs[i] + "\" must be entered numeric");
							}
						}

						break;
					case 3:
						if (cell != null && !cell.getStringCellValue().equals("")) {
							stdentDTO.setCourse(cell.getStringCellValue());
							if (!isAlphaNumeric(cell.getStringCellValue())) {
								stringBuilder.append("\"").append(HEADERs[i]
										+ "\" is invalid. Acceptable response must be an alphanumeric value");
							}
						} else {
							stringBuilder.append("\"").append(HEADERs[i] + "\" must be entered");
						}
						break;
					case 4:
						if (cell != null && cell.getCellType() == CellType.NUMERIC) {
							stdentDTO.setFee(NUMBER_FORMAT("0", cell.getNumericCellValue()));
						} else if (cell != null) {
							stdentDTO.setFee(cell.getStringCellValue());
							if (stringBuilder.length() > 0) {
								stringBuilder.append(",\"").append(HEADERs[i] + "\" must be entered numeric");
							}
						}
						break;
					default:
						break;
					}
					stdentDTO.setFailureReason(stringBuilder.toString());
				}
				studentDataList.add(stdentDTO);
			}

		} catch (IOException e) {
			log.error("Error in export data to excel", e);
			throw new CustomException("fail to parse csv/excel file: ", HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
				}
			}
		}
		return studentDataList;
	}

	private static boolean isAlphaNumeric(String name) {
//		String regex1 = "^[\\w]+$";
//		String regex2 = "^[\\w]*[\\s]{1}[\\w].*$";
		
		String regex1 = "^[a-zA-Z]+$";
		String regex2 = "[a-zA-Z]*[\\s]{1}.*";

		if (name == null ) {
			return false;
		}else if(name.matches(regex1) || name.matches(regex2)){
			return true;
		}

		return false;
	}

	private static boolean isNumeric(String name) {
		return name != null && name.matches("^[0-9]*$");
	}

	private static boolean isEmailValid(String email) {
		String regex = "[\\w]+@[\\w]+\\.[a-zA-Z]{2,3}";
		// Compile regular expression to get the pattern
		Pattern pattern = Pattern.compile(regex);
		// Create instance of matcher
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Number format.
	 *
	 * @param format the format
	 * @param value  the value
	 * @return the string
	 */
	private static String NUMBER_FORMAT(String format, Double value) {
		String number = "";
		DecimalFormat df = new DecimalFormat(format);
		number = df.format(value);
		return number;
	}

}
