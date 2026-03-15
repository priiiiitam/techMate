package com.pritam.techmate.service;

import com.pritam.techmate.entity.Subject;
import com.pritam.techmate.entity.Teacher;
import com.pritam.techmate.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    public List<Subject> getSubjectsByTeacher(Long teacherId) {
        return subjectRepository.findByTeacherTeacherId(teacherId);
    }

    public Subject createSubject(String subjectName, Integer totalStudents, Teacher teacher) {
        Subject subject = Subject.builder()
                .subjectName(subjectName)
                .totalStudents(totalStudents)
                .teacher(teacher)
                .build();
        return subjectRepository.save(subject);
    }

    public Optional<Subject> getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId);
    }

    public void deleteSubject(Long subjectId) {
        subjectRepository.deleteById(subjectId);
    }
}
