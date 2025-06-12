package com.example.scheduo.fixture

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType

fun createScheduleNotification(
    id: Long? = null,
    member: Member,
    message: String = "회의 일정이 곧 시작됩니다",
    data: Map<String, Any> = mapOf("scheduleId" to 45, "calendarId" to 3)
): Notification {
    return Notification(
        id,
        member,
        NotificationType.SCHEDULE_NOTIFICATION,
        message,
        data
    )
}

fun createCalendarInvitationNotification(
    id: Long? = null,
    member: Member,
    message: String = "새로운 캘린더 초대가 도착했습니다",
    data: Map<String, Any> = mapOf("calendarId" to 7)
): Notification {
    return Notification(
        id,
        member,
        NotificationType.CALENDAR_INVITATION,
        message,
        data
    )
}

fun createNotification(
    id: Long? = null,
    member: Member,
    type: NotificationType,
    data: Map<String, Any> = emptyMap()
): Notification {
    var message: String = ""
    when (type) {
        NotificationType.SCHEDULE_NOTIFICATION -> {
            message = NotificationType.SCHEDULE_NOTIFICATION.createMessage(data)
        }

        NotificationType.CALENDAR_INVITATION -> {
            message = NotificationType.CALENDAR_INVITATION.createMessage(data)
        }

        NotificationType.CALENDAR_INVITATION_ACCEPTED -> {
            message = NotificationType.CALENDAR_INVITATION_ACCEPTED.createMessage(data)
        }
    }
    return Notification(
        id, member, type, message, data
    )
}