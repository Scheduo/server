package com.example.scheduo.fixture

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType

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