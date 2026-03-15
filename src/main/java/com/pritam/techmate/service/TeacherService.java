package com.pritam.techmate.service;

import com.pritam.techmate.entity.Teacher;
import com.pritam.techmate.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    public Teacher getOrCreateTeacher(String email, String name) {
        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);
        if (teacherOpt.isPresent()) {
            return teacherOpt.get();
        } else {
            Teacher newTeacher = Teacher.builder()
                    .email(email)
                    .name(name)
                    .build();
            return teacherRepository.save(newTeacher);
        }
    }

    public Optional<Teacher> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }
}
