package com.example.scheduo.domain.calendar.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "calendar")
public class Calendar extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Participant> participants = new ArrayList<>();

	public void addParticipant(Participant participant) {
		this.participants.add(participant);
		participant.setCalendar(this);
	}

	public void addParticipants(List<Participant> participants) {
		for (Participant participant : participants) {
			this.addParticipant(participant);
		}
	}

	public void updateTitle(String title) {
		this.name = title;
	}

	public boolean isOwner(Long memberId) {
		return participants.stream().anyMatch(p -> p.getMember().getId().equals(memberId) && p.getRole() == Role.OWNER);
	}

	public Participant getOwner() {
		return participants.stream()
			.filter(p -> p.getRole() == Role.OWNER)
			.findFirst()
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_OWNER_NOT_FOUND));
	}

	public Optional<Participant> findParticipant(Long memberId) {
		return participants.stream()
			.filter(p -> p.getMember().getId().equals(memberId))
			.findFirst();
	}

	public Optional<Participant> findParticipantById(Long participantId) {
		return this.participants.stream()
			.filter(p -> p.getId().equals(participantId))
			.findFirst();
	}

	public void updateParticipantRole(Long participantId, Role newRole, Long requesterId) {
		// 요청자 권한 검증
		Participant requester = this.findParticipant(requesterId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		requester.validateOwnerPermission();

		// 대상 참여자 검증 및 권한 변경
		Participant targetParticipant = this.findParticipantById(participantId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		targetParticipant.validateForRoleUpdate();

		// OWNER 권한 이전 처리
		if (newRole == Role.OWNER) {
			requester.transferOwnership();
		}

		targetParticipant.updateRole(newRole);
	}

	public Participant removeParticipant(Long participantId, Long requesterId) {
		// 요청자 권한 검증
		Participant requester = this.findParticipant(requesterId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		requester.validateOwnerPermission();

		// 대상 참여자 검증
		Participant targetParticipant = this.findParticipantById(participantId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		targetParticipant.validateForRemoval();

		// 참여자 제거
		this.participants.remove(targetParticipant);

		return targetParticipant; // 삭제를 위해 반환
	}
}
