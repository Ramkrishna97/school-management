package com.schoolmanagement.timetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolmanagement.timetable.TeacherClient.TeacherAvailability;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "school.timetable.absence-scan-cron=0 0 6 * * *"
})
class SubstitutionServiceTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 16);

    @Autowired TimetableRepository timetables;
    @Autowired SubstitutionRepository substitutions;
    @Autowired SubstitutionNoticeRepository notices;
    @Autowired SubstitutionService service;
    @Autowired TimetableSchedulingService scheduling;
    @MockitoBean TeacherClient teachers;

    @BeforeEach
    void clear() {
        notices.deleteAll();
        substitutions.deleteAll();
        timetables.deleteAll();
    }

    @Test
    void selectsQualifiedFreeTeacherAndDoesNotDoubleBook() {
        timetables.save(new TimetableEntry(DayOfWeek.MONDAY, 2, "7", "A", "MATH", 1L));
        timetables.save(new TimetableEntry(DayOfWeek.MONDAY, 2, "8", "A", "MATH", 5L));
        timetables.save(new TimetableEntry(DayOfWeek.MONDAY, 2, "9", "A", "MATH", 2L));
        when(teachers.availability(MONDAY)).thenReturn(List.of(
            teacher(1, true, "MATH"), teacher(5, true, "MATH"),
            teacher(2, false, "MATH"), teacher(3, false, "MATH"),
            teacher(4, false, "ENGLISH")));

        var result = service.allocate(MONDAY);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(a -> a.substituteTeacherId()).containsExactly(3L, null);
        assertThat(result).extracting(a -> a.status()).containsExactly("ASSIGNED", "UNFILLED");
        assertThat(substitutions.findByDate(MONDAY)).hasSize(1);
        assertThat(notices.findByTeacherIdOrderByCreatedAtDesc(3L)).hasSize(1);
        assertThat(service.allocate(MONDAY)).extracting(a -> a.substituteTeacherId())
            .containsExactly(3L, null);
    }

    @Test
    void reallocatesWhenOriginalSubstituteTakesLeave() {
        timetables.save(new TimetableEntry(DayOfWeek.MONDAY, 4, "7", "B", "SCIENCE", 1L));
        when(teachers.availability(MONDAY)).thenReturn(
            List.of(teacher(1, true, "SCIENCE"), teacher(2, false, "SCIENCE"),
                teacher(3, false, "SCIENCE")),
            List.of(teacher(1, true, "SCIENCE"), teacher(2, true, "SCIENCE"),
                teacher(3, false, "SCIENCE")));

        assertThat(service.allocate(MONDAY).get(0).substituteTeacherId()).isEqualTo(2L);
        assertThat(service.allocate(MONDAY).get(0).substituteTeacherId()).isEqualTo(3L);
        assertThat(substitutions.findByDate(MONDAY)).hasSize(1);
        assertThat(notices.findByTeacherIdOrderByCreatedAtDesc(2L).get(0).getStatus())
            .isEqualTo(SubstitutionNotice.Status.CANCELLED);
        assertThat(notices.findByTeacherIdOrderByCreatedAtDesc(3L).get(0).getStatus())
            .isEqualTo(SubstitutionNotice.Status.ACTIVE);
    }

    @Test
    void dailyTimetableShowsAllPeriodsAndEffectiveTeacher() {
        timetables.save(new TimetableEntry(DayOfWeek.MONDAY, 1, "7", "A", "MATH", 1L));
        when(teachers.availability(MONDAY)).thenReturn(List.of(
            teacher(1, true, "MATH"), teacher(2, false, "MATH")));

        assertThat(scheduling.daily(MONDAY, "7", "A")).hasSize(8)
            .first().satisfies(period -> {
                assertThat(period.status()).isEqualTo("UNFILLED");
                assertThat(period.effectiveTeacherId()).isNull();
            });
        service.allocate(MONDAY);
        assertThat(scheduling.daily(MONDAY, "7", "A")).first().satisfies(period -> {
            assertThat(period.status()).isEqualTo("SUBSTITUTED");
            assertThat(period.effectiveTeacherId()).isEqualTo(2L);
        });
        assertThat(scheduling.daily(MONDAY, "7", "A").get(1).status()).isEqualTo("FREE");
    }

    private TeacherAvailability teacher(long id, boolean absent, String subject) {
        return new TeacherAvailability(id, "Teacher " + id, "General", Set.of(subject), absent);
    }
}
