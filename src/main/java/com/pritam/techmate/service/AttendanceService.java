package com.pritam.techmate.service;

import com.opencsv.CSVReader;
import com.pritam.techmate.entity.Attendance;
import com.pritam.techmate.entity.Student;
import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.repository.AttendanceRepository;
import com.pritam.techmate.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    public List<Attendance> getAttendanceBySubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectSubjectIdAndDate(subjectId, date);
    }

    public void markAttendance(Student student, Subject subject, LocalDate date, String status) {
        Optional<Attendance> existingRecord = attendanceRepository
                .findByStudentStudentIdAndSubjectSubjectIdAndDate(student.getStudentId(), subject.getSubjectId(), date);
        
        if (existingRecord.isPresent()) {
            Attendance attendance = existingRecord.get();
            attendance.setStatus(status);
            attendanceRepository.save(attendance);
        } else {
            Attendance attendance = Attendance.builder()
                    .student(student)
                    .subject(subject)
                    .date(date)
                    .status(status)
                    .build();
            attendanceRepository.save(attendance);
        }
    }
    
    public List<Attendance> getAttendanceBySubject(Long subjectId) {
        return attendanceRepository.findBySubjectSubjectId(subjectId);
    }

    public void importAttendanceFromCsv(MultipartFile file, Subject subject) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                if (row.length >= 3) {
                    try {
                        Integer rollNo = Integer.parseInt(row[0].trim());
                        final LocalDate finalDate = LocalDate.parse(row[1].trim());
                        String status = row[2].trim().toUpperCase();

                        Optional<Student> studentOpt = studentRepository.findBySubjectSubjectIdAndRollNo(subject.getSubjectId(), rollNo);
                        studentOpt.ifPresent(student -> markAttendance(student, subject, finalDate, status));
                    } catch (Exception e) {
                        // Skip invalid rows
                    }
                }
            }
        }
    }

    public void importAttendanceFromExcel(MultipartFile file, Subject subject) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null) {
                    try {
                        Integer rollNo = (int) row.getCell(0).getNumericCellValue();
                        LocalDate date = null;
                        
                        Cell dateCell = row.getCell(1);
                        if (DateUtil.isCellDateFormatted(dateCell)) {
                            date = dateCell.getLocalDateTimeCellValue().toLocalDate();
                        } else {
                            date = LocalDate.parse(dateCell.getStringCellValue().trim());
                        }

                        final LocalDate finalDate = date;
                        String status = row.getCell(2).getStringCellValue().trim().toUpperCase();

                        Optional<Student> studentOpt = studentRepository.findBySubjectSubjectIdAndRollNo(subject.getSubjectId(), rollNo);
                        studentOpt.ifPresent(student -> markAttendance(student, subject, finalDate, status));
                    } catch (Exception e) {
                        // Skip invalid rows
                    }
                }
            }
        }
    }
}
