package com.pritam.techmate.controller;

import com.pritam.techmate.service.AnalyticsService;
import com.pritam.techmate.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import com.pritam.techmate.entity.Teacher;
import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.service.TeacherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/subject/{subjectId}/analytics")
    public String viewAnalytics(@PathVariable("subjectId") Long subjectId, @AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        Teacher teacher = teacherService.findByEmail(email).orElse(null);
        Subject subject = subjectService.getSubjectById(subjectId).orElse(null);
        
        if (subject == null || teacher == null || !subject.getTeacher().getTeacherId().equals(teacher.getTeacherId())) {
            return "redirect:/dashboard";
        }

        Map<String, Object> analytics = analyticsService.getSubjectAnalytics(subjectId);

        model.addAttribute("subjectId", subjectId);
        model.addAttribute("analytics", analytics);
        return "analytics";
    }
}
