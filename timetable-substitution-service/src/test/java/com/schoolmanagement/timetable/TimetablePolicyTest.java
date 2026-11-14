package com.schoolmanagement.timetable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TimetablePolicyTest {
    private final TimetablePolicy policy = new TimetablePolicy(8, 6, 3, "unused");

    @Test
    void reservesFreePeriodsAndBreaks() {
        assertThat(policy.canAddPeriod(Set.of(1, 2, 3), 4)).isFalse();
        assertThat(policy.canAddPeriod(Set.of(1, 2, 4), 3)).isFalse();
        assertThat(policy.canAddPeriod(Set.of(1, 2, 5, 6, 7, 8), 4)).isFalse();
        assertThat(policy.canAddPeriod(Set.of(1, 2, 5), 3)).isTrue();
        assertThat(policy.canAddPeriod(Set.of(2, 5), 2)).isFalse();
    }
}
