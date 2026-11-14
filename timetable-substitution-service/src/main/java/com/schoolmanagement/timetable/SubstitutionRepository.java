package com.schoolmanagement.timetable;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubstitutionRepository extends JpaRepository<Substitution, Long> {
    List<Substitution> findByDate(LocalDate date);
}
