package com.schoolmanagement.auth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParentUserId(Long parentUserId);
    boolean existsByParentUserIdAndStudentId(Long parentUserId, Long studentId);
}
