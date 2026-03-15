package com.pritam.techmate.repository;

import com.pritam.techmate.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySubjectSubjectIdAndDate(Long subjectId, LocalDate date);
    Optional<Attendance> findByStudentStudentIdAndSubjectSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);
    
    List<Attendance> findByStudentStudentId(Long studentId);
    List<Attendance> findBySubjectSubjectId(Long subjectId);

    long countByStudentStudentIdAndStatus(Long studentId, String status);
    long countByStudentStudentIdAndStatusAndDateBetween(Long studentId, String status, LocalDate start, LocalDate end);
    long countByStudentStudentIdAndDateBetween(Long studentId, LocalDate start, LocalDate end);
}
