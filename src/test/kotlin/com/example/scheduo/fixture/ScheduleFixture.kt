package com.example.scheduo.fixture

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.schedule.entity.Category
import com.example.scheduo.domain.schedule.entity.NotificationTime
import com.example.scheduo.domain.schedule.entity.Recurrence
import com.example.scheduo.domain.schedule.entity.Schedule

fun createSchedule(
        title: String = "테스트 일정",
        startDate: String = "2025-07-01",
        endDate: String = "2025-07-01",
        startTime: String = "09:00",
        endTime: String = "10:00",
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
        startDate,
        endDate,
        startTime,
        endTime,
        location,
        memo,
        NotificationTime.ONE_DAY_BEFORE,
        category,
        member,
        calendar,
        recurrence
)
