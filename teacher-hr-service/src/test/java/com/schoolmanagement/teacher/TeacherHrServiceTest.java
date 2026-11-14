package com.schoolmanagement.teacher;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
class TeacherHrServiceTest {
    @Autowired TeacherHrService service;

    @Test
    void pendingLeaveIsNotAbsenceUntilApprovedAndBalanceIsDeducted() {
        LocalDate date = LocalDate.of(2030, 1, 8);
        assertThat(service.availability(date)).filteredOn(t -> t.id() == 4L)
            .singleElement().satisfies(t -> assertThat(t.absent()).isFalse());

        var leave = service.decide(2L,
            new TeacherApi.DecideLeave(TeacherLeave.Status.APPROVED, "Approved by principal"));

        assertThat(leave.getStatus()).isEqualTo(TeacherLeave.Status.APPROVED);
        assertThat(service.availability(date)).filteredOn(t -> t.id() == 4L)
            .singleElement().satisfies(t -> assertThat(t.absent()).isTrue());
        assertThat(service.get(4L).casualLeaveBalance()).isEqualTo(10);
    }
}
