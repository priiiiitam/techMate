package com.pritam.techmate.controller;

import com.pritam.techmate.entity.Attendance;
import com.pritam.techmate.entity.Student;
import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.service.AttendanceService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

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

    @GetMapping("/subject/{subjectId}/attendance")
    public String viewAttendance(@PathVariable("subjectId") Long subjectId,
                                 @RequestParam(value = "date", required = false) String date,
                                 @AuthenticationPrincipal OAuth2User principal,
                                 Model model) {
        
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) {
            return "redirect:/dashboard";
        }

        LocalDate selectedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();

        List<Student> students = studentService.getStudentsBySubject(subjectId);
        List<Attendance> attendances = attendanceService.getAttendanceBySubjectAndDate(subjectId, selectedDate);

        // Convert List to a Map for much easier Thymeleaf rendering
        Map<Long, String> attendanceMap = attendances.stream()
                .collect(java.util.stream.Collectors.toMap(
                        att -> att.getStudent().getStudentId(),
                        Attendance::getStatus
                ));

        model.addAttribute("subject", subject);
        model.addAttribute("students", students);
        model.addAttribute("attendanceMap", attendanceMap);
        model.addAttribute("selectedDate", selectedDate);

        return "attendance";
    }

    @PostMapping("/subject/{subjectId}/attendance/save")
    public String saveAttendance(@PathVariable("subjectId") Long subjectId,
                                 @RequestParam("dateStr") String dateStr,
                                 @RequestParam Map<String, String> allParams,
                                 @AuthenticationPrincipal OAuth2User principal,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) {
            return "redirect:/dashboard";
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("status_")) {
                    Long studentId = Long.parseLong(entry.getKey().substring(7));
                    String status = entry.getValue();

                    studentService.getStudentsBySubject(subjectId).stream()
                            .filter(s -> s.getStudentId().equals(studentId))
                            .findFirst()
                            .ifPresent(student -> attendanceService.markAttendance(student, subject, date, status));
                }
            }
            redirectAttributes.addFlashAttribute("success", "Attendance for " + dateStr + " saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving attendance: " + e.getMessage());
        }

        return "redirect:/subject/" + subjectId + "/attendance?date=" + dateStr;
    }

    @PostMapping("/subject/{subjectId}/attendance/import")
    public String importAttendance(@PathVariable("subjectId") Long subjectId,
                                    @RequestParam("file") MultipartFile file,
                                    @AuthenticationPrincipal OAuth2User principal,
                                    RedirectAttributes redirectAttributes) {

        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (!isOwner(principal, subject)) return "redirect:/dashboard";

        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.endsWith(".csv")) {
                    attendanceService.importAttendanceFromCsv(file, subject);
                    redirectAttributes.addFlashAttribute("success", "Attendance CSV Imported successfully.");
                } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                    attendanceService.importAttendanceFromExcel(file, subject);
                    redirectAttributes.addFlashAttribute("success", "Attendance Excel Imported successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Invalid file format.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing attendance file: " + e.getMessage());
        }

        return "redirect:/subject/" + subjectId;
    }
}
