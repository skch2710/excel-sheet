package com.springboot.excelsheet.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.springboot.excelsheet.dto.StudentDTO;
import com.springboot.excelsheet.model.Student;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ObjectMapper {

	ObjectMapper INSTANCE = Mappers.getMapper(ObjectMapper.class);

	StudentDTO fromStudent(Student student);
	List<StudentDTO> fromStudent(List<Student> student);
}
