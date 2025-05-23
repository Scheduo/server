package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.Participant
import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.entity.Role
import com.example.scheduo.domain.member.entity.Member


fun createParticipant(
    id: Long? = null,
    nickname: String? = null,
    role: Role = Role.VIEW,
    calendar: Calendar,
    member: Member,
    participationStatus: ParticipationStatus = ParticipationStatus.PENDING
): Participant {
    return Participant(
        id,
        nickname,
        role,
        calendar,
        member,
        participationStatus
    )
}
