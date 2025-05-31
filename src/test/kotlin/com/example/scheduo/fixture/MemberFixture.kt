package com.example.scheduo.fixture

import com.example.scheduo.domain.member.dto.MemberRequestDto
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.SocialType

fun createMember(
    id: Long? = null,
    email: String = "test@gmail.com",
    nickname: String? = null,
    socialType: SocialType = SocialType.GOOGLE
): Member {
    return Member(
            id,
            email,
            nickname,
            socialType,
    )
}

fun createEditInfoRequest(nickname: String = "홍길동"): MemberRequestDto.EditInfo {
    return MemberRequestDto.EditInfo(nickname)
}
