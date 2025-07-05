package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.Participant


fun createCalendar(
    id: Long? = null,
    name: String = "캘린더 이름",
    participants: MutableList<Participant> = mutableListOf()
): Calendar {
    return Calendar(
        id,
        name,
        participants
    )
}