package com.springboot.excelsheet.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.excelsheet.model.Student;

public interface StudentDAO extends JpaRepository<Student, Long> {
	
	Student findByEmailId(String emailId);

}
