package com.example.scheduo.fixture

import com.example.scheduo.domain.schedule.entity.Recurrence

fun createRecurrence(
        frequency: String = "WEEKLY",
        recurrenceEndDate: String = "2025-12-31"
) = Recurrence.create(
        frequency,
        recurrenceEndDate
)