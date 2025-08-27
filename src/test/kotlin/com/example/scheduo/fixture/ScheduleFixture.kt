package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.schedule.entity.Category
import com.example.scheduo.domain.schedule.entity.NotificationTime
import com.example.scheduo.domain.schedule.entity.Recurrence
import com.example.scheduo.domain.schedule.entity.Schedule
import java.time.LocalDateTime

fun createSchedule(
    title: String = "테스트 일정",
    start: LocalDateTime = LocalDateTime.of(2025, 7, 1, 9, 0),
    end: LocalDateTime = LocalDateTime.of(2025, 7, 1, 10, 0),
    isAllDay: Boolean = false,
    location: String = "회의실",
    memo: String = "테스트 메모",
    member: Member,
    calendar: Calendar,
    category: Category,
    recurrence: Recurrence? = null
) = Schedule.create(
    title,
    isAllDay,
    start,
    end,
    location,
    memo,
    NotificationTime.ONE_DAY_BEFORE,
    category,
    member,
    calendar,
    recurrence
)
