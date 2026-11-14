package com.schoolmanagement.timetable;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "school.timetable")
public record TimetablePolicy(int periodsPerDay, int maxTeachingPeriodsPerDay,
                              int maxConsecutivePeriods, String absenceScanCron) {
    public TimetablePolicy {
        if (periodsPerDay < 1 || maxTeachingPeriodsPerDay < 1 ||
            maxTeachingPeriodsPerDay >= periodsPerDay || maxConsecutivePeriods < 1) {
            throw new IllegalArgumentException("Invalid timetable policy: reserve at least one free period per day");
        }
    }

    public boolean canAddPeriod(Set<Integer> occupied, int period) {
        if (period < 1 || period > periodsPerDay || occupied.contains(period) ||
            occupied.size() >= maxTeachingPeriodsPerDay) return false;
        int consecutive = 1;
        for (int before = period - 1; occupied.contains(before); before--) consecutive++;
        for (int after = period + 1; occupied.contains(after); after++) consecutive++;
        return consecutive <= maxConsecutivePeriods;
    }
}
