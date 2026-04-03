package com.pritam.techmate.controller;

import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.entity.Teacher;
import com.pritam.techmate.service.SubjectService;
import com.pritam.techmate.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SubjectController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        Teacher teacher = teacherService.getOrCreateTeacher(email, name);
        List<Subject> subjects = subjectService.getSubjectsByTeacher(teacher.getTeacherId());

        model.addAttribute("teacher", teacher);
        model.addAttribute("subjects", subjects);

        return "dashboard"; // Teacher dashboard showing all subjects
    }

    @PostMapping("/create-subject")
    public String createSubject(@AuthenticationPrincipal OAuth2User principal,
                                @RequestParam("subjectName") String subjectName,
                                @RequestParam("count") int count) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        Teacher teacher = teacherService.findByEmail(email).orElse(null);

        if (teacher != null) {
            subjectService.createSubject(subjectName, count, teacher);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/subject/{id}")
    public String subjectDashboard(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        Teacher teacher = teacherService.findByEmail(email).orElse(null);
        Subject subject = subjectService.getSubjectById(id).orElse(null);
        
        if (subject == null || teacher == null || !subject.getTeacher().getTeacherId().equals(teacher.getTeacherId())) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("subject", subject);
        return "subject"; // The specific subject dashboard
    }

    @PostMapping("/subject/{id}/delete")
    public String deleteSubject(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        
        // Ownership check
        String email = principal.getAttribute("email");
        Teacher teacher = teacherService.findByEmail(email).orElse(null);
        Subject subject = subjectService.getSubjectById(id).orElse(null);

        if (teacher != null && subject != null && subject.getTeacher().getTeacherId().equals(teacher.getTeacherId())) {
            subjectService.deleteSubject(id);
        }

        return "redirect:/dashboard";
    }
}