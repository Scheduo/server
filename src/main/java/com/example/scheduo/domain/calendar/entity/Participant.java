package com.example.scheduo.domain.calendar.entity;

import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "participant")
public class Participant extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String nickname;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendarId")
	private Calendar calendar;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId")
	private Member member;

	@Setter
	@Enumerated(EnumType.STRING)
	private ParticipationStatus status;

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void reinvite() {
		switch (this.status) {
			case DECLINED -> this.status = ParticipationStatus.PENDING;
			case PENDING -> throw new ApiException(ResponseStatus.MEMBER_ALREADY_INVITED);
			case ACCEPTED -> throw new ApiException(ResponseStatus.MEMBER_ALREADY_PARTICIPANT);
		}
	}

	public void accept() {
		switch (this.status) {
			case PENDING -> this.status = ParticipationStatus.ACCEPTED;
			case ACCEPTED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_ACCEPTED);
			case DECLINED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_DECLINED);
		}
	}

	public void decline() {
		switch (this.status) {
			case PENDING -> this.status = ParticipationStatus.DECLINED;
			case ACCEPTED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_ACCEPTED);
			case DECLINED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_DECLINED);
		}
	}

	public void transferOwnership() {
		this.role = Role.EDIT;
	}

	public void updateRole(Role newRole) {
		this.role = newRole;
	}

	public void validateOwnerPermission() {
		if (this.role != Role.OWNER) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_OWNER);
		}
	}

	public void validateForRoleUpdate() {
		if (this.status != ParticipationStatus.ACCEPTED) {
			throw new ApiException(ResponseStatus.PARTICIPANT_NOT_ACCEPTED);
		}
	}
}
