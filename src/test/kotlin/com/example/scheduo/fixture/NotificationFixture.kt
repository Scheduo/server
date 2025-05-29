package com.example.scheduo.fixture

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType

fun createScheduleNotification(
        id: Long? = null,
        member: Member,
        title: String = "회의 일정이 곧 시작됩니다",
        data: Map<String, Any> = mapOf("scheduleId" to 45, "calendarId" to 3),
): Notification {
    return Notification(
            id,
            member,
            NotificationType.SCHEDULE_NOTIFICATION,
            title,
            data
    )
}

fun createCalendarInvitationNotification(
        id: Long? = null,
        member: Member,
        title: String = "새로운 캘린더 초대가 도착했습니다",
        data: Map<String, Any> = mapOf("calendarId" to 7),
): Notification {
    return Notification(
            id,
            member,
            NotificationType.CALENDAR_INVITATION,
            title,
            data
    )
}
