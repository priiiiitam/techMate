package com.pritam.techmate.service;

import com.pritam.techmate.entity.Attendance;
import com.pritam.techmate.entity.Student;
import com.pritam.techmate.entity.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    public Map<String, Object> getSubjectAnalytics(Long subjectId) {
        Map<String, Object> analytics = new HashMap<>();

        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        if (subject == null) {
            return analytics;
        }

        List<Student> students = studentService.getStudentsBySubject(subjectId);
        List<Attendance> allAttendance = attendanceService.getAttendanceBySubject(subjectId);

        int totalClasses = (int) allAttendance.stream().map(Attendance::getDate).distinct().count();
        int totalStudents = students.size();

        int totalPresent = 0;
        int totalAbsent = 0;
        int totalAuthorizedLeave = 0;

        for (Attendance a : allAttendance) {
            switch (a.getStatus()) {
                case "P": totalPresent++; break;
                case "A": totalAbsent++; break;
                case "AL": totalAuthorizedLeave++; break;
            }
        }

        double overallAttendancePercentage = 0;
        if (totalClasses > 0 && totalStudents > 0) {
           int totalPossibleAttendances = totalClasses * totalStudents;
           overallAttendancePercentage = (double) totalPresent / (totalPossibleAttendances - totalAuthorizedLeave) * 100;
        }

        int studentsBelow75 = 0;
        
        // Calculate per-student stats
        Map<String, Double> studentAttendanceMap = new HashMap<>();
        for (Student s : students) {
            long presentCount = allAttendance.stream()
                .filter(a -> a.getStudent().getStudentId().equals(s.getStudentId()) && a.getStatus().equals("P"))
                .count();
            
            long authLeaveCount = allAttendance.stream()
                .filter(a -> a.getStudent().getStudentId().equals(s.getStudentId()) && a.getStatus().equals("AL"))
                .count();

            int effectiveClasses = totalClasses - (int)authLeaveCount;
            double percentage = effectiveClasses > 0 ? ((double) presentCount / effectiveClasses) * 100 : 0.0;
            
            studentAttendanceMap.put(s.getName(), percentage);
            
            if (percentage < 75.0 && effectiveClasses > 0) {
                studentsBelow75++;
            }
        }

        // Monthly Trend
        Map<String, Integer> monthlyTrend = new HashMap<>();
        for(Attendance a : allAttendance) {
            if(a.getStatus().equals("P")) {
                String month = a.getDate().getMonth().toString();
                monthlyTrend.put(month, monthlyTrend.getOrDefault(month, 0) + 1);
            }
        }


        analytics.put("totalClasses", totalClasses);
        analytics.put("totalStudents", totalStudents);
        analytics.put("overallAttendancePercentage", String.format("%.1f", overallAttendancePercentage));
        analytics.put("totalPresent", totalPresent);
        analytics.put("totalAbsent", totalAbsent);
        analytics.put("studentsBelow75", studentsBelow75);
        analytics.put("studentAttendanceMap", studentAttendanceMap);
        analytics.put("monthlyTrend", monthlyTrend);

        return analytics;
    }
}
