package com.schoolmanagement.teacher;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public final class TeacherApi {
    private TeacherApi() {}

    public record CreateTeacher(@NotBlank String name, @NotBlank String department,
                                @NotBlank String qualification, String classTeacherOf,
                                @NotEmpty Set<@NotBlank String> subjectCodes) {}

    public record TeacherView(Long id, String name, String department, String qualification,
                              String classTeacherOf, Set<String> subjectCodes,
                              int casualLeaveBalance, int sickLeaveBalance, int earnedLeaveBalance) {
        static TeacherView from(Teacher teacher) {
            return new TeacherView(teacher.getId(), teacher.getName(), teacher.getDepartment(),
                teacher.getQualification(), teacher.getClassTeacherOf(), teacher.getSubjectCodes(),
                teacher.getCasualLeaveBalance(), teacher.getSickLeaveBalance(),
                teacher.getEarnedLeaveBalance());
        }
    }

    public record Availability(Long id, String name, String department,
                               Set<String> subjectCodes, boolean absent) {}

    public record ApplyLeave(@NotNull @FutureOrPresent LocalDate startDate, @NotNull LocalDate endDate,
                             @NotNull TeacherLeave.Type type, @NotBlank String reason) {}

    public record DecideLeave(@NotNull TeacherLeave.Status status, String note) {}

    public record DecisionResult(TeacherLeave leave, boolean allocationTriggered) {}
}
