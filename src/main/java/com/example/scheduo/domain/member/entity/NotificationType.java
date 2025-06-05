package com.example.scheduo.domain.member.entity;

import java.util.Map;

public enum NotificationType {
	CALENDAR_INVITATION {
		@Override
		public String createMessage(Map<String, Object> data) {
			String inviterName = (String)data.get("inviterName");
			String calendarName = (String)data.get("calendarName");
			return String.format("%s 님이 %s 캘린더에 초대했습니다.", inviterName, calendarName);
		}

		@Override
		public Map<String, Object> createData(Map<String, Object> data) {
			return Map.of(
				"calendarId", data.get("calendarId")
			);
		}
	},
	SCHEDULE_NOTIFICATION {
		@Override
		public String createMessage(Map<String, Object> data) {
			return "일정 알림";
		}

		@Override
		public Map<String, Object> createData(Map<String, Object> data) {
			return data;
		}
	},
	CALENDAR_INVITATION_ACCEPTED {
		@Override
		public String createMessage(Map<String, Object> data) {
			String inviteeNickname = (String)data.get("inviteeNickname");
			String calendarName = (String)data.get("calendarName");
			return String.format("%s 님이 %s 캘린더 초대를 수락했습니다.", inviteeNickname, calendarName);
		}

		@Override
		public Map<String, Object> createData(Map<String, Object> data) {
			return Map.of();
		}
	};

	public abstract String createMessage(Map<String, Object> data);

	public abstract Map<String, Object> createData(Map<String, Object> data);
}
