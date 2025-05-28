package com.example.scheduo.fixture

import com.example.scheduo.domain.member.dto.MemberRequestDto
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.SocialType

fun createMemberGOOGLE(
        id: Long? = null,
        email: String = "test@example.com",
        nickname: String? = "테스트닉네임",
        socialType: SocialType = SocialType.GOOGLE
): Member {
    return Member(id, email, nickname, socialType)
}

fun createEditInfoRequest(nickname: String = "홍길동"): MemberRequestDto.EditInfo {
    return MemberRequestDto.EditInfo(nickname)
}