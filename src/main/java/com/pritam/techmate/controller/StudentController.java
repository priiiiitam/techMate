package com.pritam.techmate.controller;

import com.pritam.techmate.entity.Student;
import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.service.StudentService;
import com.pritam.techmate.service.SubjectService;
import com.pritam.techmate.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    private boolean isOwner(OAuth2User principal, Subject subject) {
        if (principal == null || subject == null) return false;
        String email = principal.getAttribute("email");
        com.pritam.techmate.entity.Teacher teacher = teacherService.findByEmail(email).orElse(null);
        return teacher != null && subject.getTeacher().getTeacherId().equals(teacher.getTeacherId());
    }

    @PostMapping("/subject/{subjectId}/add-student")
    public String addStudentManual(@PathVariable("subjectId") Long subjectId,
                                   @RequestParam("name") String name,
                                   @RequestParam("rollNo") Integer rollNo,
                                   @AuthenticationPrincipal OAuth2User principal,
                                   RedirectAttributes redirectAttributes) {

        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) return "redirect:/dashboard";

        boolean added = studentService.addStudentManual(name, rollNo, subject);
        if (added) {
            redirectAttributes.addFlashAttribute("success", "Student added successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: A student with Roll No " + rollNo + " already exists in this subject.");
        }
        
        return "redirect:/subject/" + subjectId;
    }

    @GetMapping("/subject/{subjectId}/search-student")
    public String searchStudent(@PathVariable("subjectId") Long subjectId,
                                @RequestParam("query") String query,
                                @AuthenticationPrincipal OAuth2User principal,
                                Model model) {
        
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) return "redirect:/dashboard";

        List<Student> results = studentService.searchStudents(subjectId, query);
        
        boolean isRollSearch = false;
        try {
            Integer.parseInt(query);
            isRollSearch = true;
        } catch (NumberFormatException ignored) {}

        if (isRollSearch && !results.isEmpty()) {
            Student student = results.get(0);
            model.addAttribute("statPresent", studentService.getTotalPresentDays(student.getStudentId()));
            model.addAttribute("statAvg", String.format("%.2f", studentService.getMonthlyAverage(student.getStudentId())));
        }

        model.addAttribute("subject", subject);
        model.addAttribute("searchResults", results);
        model.addAttribute("isRollSearch", isRollSearch);
        
        return "subject";
    }

    @PostMapping("/subject/{subjectId}/delete-student/{studentId}")
    public String deleteStudent(@PathVariable("subjectId") Long subjectId,
                                @PathVariable("studentId") Long studentId,
                                @AuthenticationPrincipal OAuth2User principal,
                                RedirectAttributes redirectAttributes) {
        
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) return "redirect:/dashboard";

        studentService.deleteStudent(studentId);
        redirectAttributes.addFlashAttribute("success", "Student deleted successfully.");
        return "redirect:/subject/" + subjectId;
    }

    @GetMapping("/subject/{subjectId}/export")
    public void exportAttendance(@PathVariable("subjectId") Long subjectId,
                                 @RequestParam("period") String period,
                                 @RequestParam(value = "date", required = false) String dateStr,
                                 @RequestParam(value = "month", required = false) String monthStr,
                                 @RequestParam("format") String format,
                                 @AuthenticationPrincipal OAuth2User principal,
                                 jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) {
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        java.time.LocalDate start, end;

        if ("daily".equals(period) && dateStr != null && !dateStr.isEmpty()) {
            start = java.time.LocalDate.parse(dateStr);
            end = start;
        } else if ("monthly".equals(period) && monthStr != null && !monthStr.isEmpty()) {
            java.time.YearMonth ym = java.time.YearMonth.parse(monthStr);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        } else {
            start = java.time.LocalDate.now();
            end = start;
        }

        String subjectName = subject.getSubjectName().replaceAll("\\s+", "_");
        String periodLabel = start.equals(end) ? start.toString() : start + "_to_" + end;

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + subjectName + "_" + periodLabel + ".csv\"");
            response.getWriter().write(studentService.generateExportCsv(subjectId, start, end));
        } else if ("excel".equalsIgnoreCase(format)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + subjectName + "_" + periodLabel + ".xlsx\"");
            try (org.apache.poi.ss.usermodel.Workbook workbook = studentService.generateExportWorkbook(subjectId, start, end)) {
                workbook.write(response.getOutputStream());
            }
        } else {
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST, "Invalid format");
        }
    }

    @PostMapping("/subject/{subjectId}/import-students")
    public String importStudents(@PathVariable("subjectId") Long subjectId,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal OAuth2User principal,
                                 RedirectAttributes redirectAttributes) {
                                     
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) {
            return "redirect:/dashboard";
        }

        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.endsWith(".csv")) {
                    studentService.importStudentsFromCsv(file, subject);
                    redirectAttributes.addFlashAttribute("success", "CSV Imported successfully.");
                } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                    studentService.importStudentsFromExcel(file, subject);
                    redirectAttributes.addFlashAttribute("success", "Excel Imported successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Invalid file format.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing file: " + e.getMessage());
        }

        return "redirect:/subject/" + subjectId;
    }
}
