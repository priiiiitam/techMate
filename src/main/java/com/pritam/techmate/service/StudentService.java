package com.pritam.techmate.service;

import com.opencsv.CSVReader;
import com.pritam.techmate.entity.Student;
import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.repository.StudentRepository;
import com.pritam.techmate.repository.SubjectRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public List<Student> getStudentsBySubject(Long subjectId) {
        return studentRepository.findBySubjectSubjectIdOrderByRollNoAsc(subjectId);
    }

    public boolean addStudentManual(String name, Integer rollNo, Subject subject) {
        Optional<Student> existingStudent = studentRepository.findBySubjectSubjectIdAndRollNo(subject.getSubjectId(), rollNo);
        if (existingStudent.isEmpty()) {
            Student student = Student.builder()
                    .name(name)
                    .rollNo(rollNo)
                    .subject(subject)
                    .build();
            studentRepository.save(student);
            
            long exactCount = studentRepository.countBySubjectSubjectId(subject.getSubjectId());
            subject.setTotalStudents((int) exactCount);
            subjectRepository.save(subject);
            
            return true;
        }
        return false;
    }

    public void importStudentsFromCsv(MultipartFile file, Subject subject) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                if (row.length >= 2) {
                    String col1 = row[0].trim();
                    String col2 = row[1].trim();

                    Integer rollNo = null;
                    String name = null;

                    try {
                        rollNo = Integer.parseInt(col1);
                        name = col2;
                    } catch (NumberFormatException e1) {
                        try {
                            // Maybe the user put Name first, RollNo second
                            rollNo = Integer.parseInt(col2);
                            name = col1;
                        } catch (NumberFormatException e2) {
                            // If neither is a valid number, it's a header or invalid row. Safely skip!
                            continue;
                        }
                    }

                    if (rollNo != null && name != null && !name.isEmpty()) {
                        addStudentManual(name, rollNo, subject);
                    }
                }
            }
        }
    }

    public void importStudentsFromExcel(MultipartFile file, Subject subject) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getCell(0) != null && row.getCell(1) != null) {
                    Cell cell0 = row.getCell(0);
                    Cell cell1 = row.getCell(1);

                    Integer rollNo = null;
                    String name = null;

                    // Try to parse cell0 as RollNo, cell1 as Name
                    if (cell0.getCellType() == CellType.NUMERIC) {
                        rollNo = (int) cell0.getNumericCellValue();
                        name = getCellStringValue(cell1);
                    } else if (cell1.getCellType() == CellType.NUMERIC) {
                        // Or maybe cell1 is RollNo, cell0 is Name
                        rollNo = (int) cell1.getNumericCellValue();
                        name = getCellStringValue(cell0);
                    } else {
                        // If both are strings, try parsing them
                        String str0 = getCellStringValue(cell0);
                        String str1 = getCellStringValue(cell1);

                        try {
                            rollNo = Integer.parseInt(str0);
                            name = str1;
                        } catch (NumberFormatException e1) {
                            try {
                                rollNo = Integer.parseInt(str1);
                                name = str0;
                            } catch (NumberFormatException e2) {
                                // Neither is a valid number, safely skip this row (likely a header)
                                continue;
                            }
                        }
                    }

                    if (rollNo != null && name != null && !name.isEmpty()) {
                        addStudentManual(name, rollNo, subject);
                    }
                }
            }
        }
    }

    public Optional<Student> getStudentById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    public void deleteStudent(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            Subject subject = student.getSubject();
            
            studentRepository.deleteById(studentId);
            
            if (subject != null) {
                long exactCount = studentRepository.countBySubjectSubjectId(subject.getSubjectId());
                subject.setTotalStudents((int) exactCount);
                subjectRepository.save(subject);
            }
        }
    }

    public List<Student> searchStudents(Long subjectId, String query) {
        try {
            Integer rollNo = Integer.parseInt(query);
            Optional<Student> student = studentRepository.findBySubjectSubjectIdAndRollNo(subjectId, rollNo);
            return student.map(List::of).orElse(new ArrayList<>());
        } catch (NumberFormatException e) {
            return studentRepository.findBySubjectSubjectIdAndNameContainingIgnoreCase(subjectId, query);
        }
    }

    @Autowired
    private com.pritam.techmate.repository.AttendanceRepository attendanceRepository;

    public long getTotalPresentDays(Long studentId) {
        return attendanceRepository.countByStudentStudentIdAndStatus(studentId, "P");
    }

    public double getMonthlyAverage(Long studentId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate start = now.withDayOfMonth(1);
        java.time.LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        
        long present = attendanceRepository.countByStudentStudentIdAndStatusAndDateBetween(studentId, "P", start, end);
        long total = attendanceRepository.countByStudentStudentIdAndDateBetween(studentId, start, end);
        
        if (total == 0) return 0.0;
        return (double) present / total * 100;
    }

    public String generateCsvContent(Long subjectId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return generateExportCsv(subjectId, now, now);
    }

    public Workbook generateExcelWorkbook(Long subjectId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return generateExportWorkbook(subjectId, now, now);
    }

    /**
     * Generate CSV from DB data for the given period (start inclusive, end inclusive).
     * Fields: Roll No, Name, Attendance Average (%) for the period.
     */
    public String generateExportCsv(Long subjectId, java.time.LocalDate start, java.time.LocalDate end) {
        List<Student> students = getStudentsBySubject(subjectId);
        String period = start.equals(end)
                ? start.toString()
                : start.toString() + " to " + end.toString();

        StringBuilder csv = new StringBuilder("Period,Roll No,Name,Attendance Average (%)\n");
        for (Student s : students) {
            long present = attendanceRepository.countByStudentStudentIdAndStatusAndDateBetween(s.getStudentId(), "P", start, end);
            long total = attendanceRepository.countByStudentStudentIdAndDateBetween(s.getStudentId(), start, end);
            double avg = total == 0 ? 0.0 : (double) present / total * 100;
            csv.append(period).append(",")
               .append(s.getRollNo()).append(",")
               .append(s.getName()).append(",")
               .append(String.format("%.2f", avg))
               .append("\n");
        }
        return csv.toString();
    }

    /**
     * Generate Excel Workbook from DB data for the given period.
     */
    public Workbook generateExportWorkbook(Long subjectId, java.time.LocalDate start, java.time.LocalDate end) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");
        String period = start.equals(end)
                ? start.toString()
                : start.toString() + " to " + end.toString();

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Period");
        header.createCell(1).setCellValue("Roll No");
        header.createCell(2).setCellValue("Name");
        header.createCell(3).setCellValue("Attendance Average (%)");

        List<Student> students = getStudentsBySubject(subjectId);
        int rowIdx = 1;
        for (Student s : students) {
            long present = attendanceRepository.countByStudentStudentIdAndStatusAndDateBetween(s.getStudentId(), "P", start, end);
            long total = attendanceRepository.countByStudentStudentIdAndDateBetween(s.getStudentId(), start, end);
            double avg = total == 0 ? 0.0 : (double) present / total * 100;

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(period);
            row.createCell(1).setCellValue(s.getRollNo());
            row.createCell(2).setCellValue(s.getName());
            row.createCell(3).setCellValue(avg);
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }
}
