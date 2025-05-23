package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.Participant
import com.example.scheduo.domain.member.entity.Member


fun createCalendar(
    id: Long? = null,
    name: String = "캘린더 이름",
    member: Member = createMember(),
    isShared: Boolean = false,
    participants: List<Participant> = emptyList()
): Calendar {
    return Calendar(
        id,
        member,
        name,
        isShared,
        participants
    )
}