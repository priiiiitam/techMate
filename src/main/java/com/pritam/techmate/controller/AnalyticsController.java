package com.pritam.techmate.controller;

import com.pritam.techmate.service.AnalyticsService;
import com.pritam.techmate.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/subject/{subjectId}/analytics")
    public String viewAnalytics(@PathVariable("subjectId") Long subjectId, Model model) {
        if (subjectService.getSubjectById(subjectId).isEmpty()) {
            return "redirect:/dashboard";
        }

        Map<String, Object> analytics = analyticsService.getSubjectAnalytics(subjectId);

        model.addAttribute("subjectId", subjectId);
        model.addAttribute("analytics", analytics);
        return "analytics";
    }
}
