package com.schoolmanagement.timetable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;

public final class TimetableApi {
    private TimetableApi() {}

    public record CreateEntry(@NotNull DayOfWeek dayOfWeek, @Min(1) int periodNumber,
                              @NotBlank String className, @NotBlank String section,
                              @NotBlank String subjectCode, @NotNull Long teacherId) {}

    public record EntryView(Long id, DayOfWeek dayOfWeek, int periodNumber, String className,
                            String section, String subjectCode, Long teacherId) {
        static EntryView from(TimetableEntry entry) {
            return new EntryView(entry.getId(), entry.getDayOfWeek(), entry.getPeriodNumber(),
                entry.getClassName(), entry.getSection(), entry.getSubjectCode(), entry.getTeacherId());
        }
    }

    public record Allocation(Long timetableEntryId, int periodNumber, String className,
                             String section, String subjectCode, Long absentTeacherId,
                             Long substituteTeacherId, String status) {
        static Allocation assigned(Substitution substitution) {
            TimetableEntry entry = substitution.getEntry();
            return new Allocation(entry.getId(), entry.getPeriodNumber(), entry.getClassName(),
                entry.getSection(), entry.getSubjectCode(), entry.getTeacherId(),
                substitution.getSubstituteTeacherId(), "ASSIGNED");
        }
        static Allocation unfilled(TimetableEntry entry) {
            return new Allocation(entry.getId(), entry.getPeriodNumber(), entry.getClassName(),
                entry.getSection(), entry.getSubjectCode(), entry.getTeacherId(), null, "UNFILLED");
        }
    }

    public record Workload(Long teacherId, LocalDate date, int scheduledPeriods,
                           int substitutionPeriods, int totalPeriods, int freePeriods) {}

    public record DailyPeriod(LocalDate date, int periodNumber, String className, String section,
                              String subjectCode, Long scheduledTeacherId,
                              Long effectiveTeacherId, String status) {}
}
