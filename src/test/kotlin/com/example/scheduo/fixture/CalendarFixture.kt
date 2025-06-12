package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.Participant
import com.example.scheduo.domain.schedule.entity.Schedule


fun createCalendar(
    id: Long? = null,
    name: String = "캘린더 이름",
    participants: List<Participant> = emptyList(),
    schedules: List<Schedule> = emptyList()
): Calendar {
    return Calendar(
        id,
        name,
        participants,
        schedules
    )
}