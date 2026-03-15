package com.pritam.techmate.repository;

import com.pritam.techmate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySubjectSubjectId(Long subjectId);
    Optional<Student> findBySubjectSubjectIdAndRollNo(Long subjectId, Integer rollNo);
    List<Student> findBySubjectSubjectIdAndNameContainingIgnoreCase(Long subjectId, String name);
}
