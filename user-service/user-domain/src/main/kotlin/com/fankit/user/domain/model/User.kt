package com.fankit.user.domain.model

data class User(
    val id: Long? = null,
    val email: Email,
    val password: EncodedPassword,
    val nickname: String,
    val role: Role,
) {
    fun promoteToCreator(): User {
        require(role == Role.USER) { "이미 ${role}인 사용자는 크리에이터로 전환할 수 없다" }
        return copy(role = Role.CREATOR)
    }

    companion object {
        fun create(email: Email, password: EncodedPassword, nickname: String): User {
            require(nickname.isNotBlank()) { "닉네임은 비어있을 수 없다" }
            require(nickname.length <= 50) { "닉네임은 50자 이하여야 한다" }
            return User(email = email, password = password, nickname = nickname, role = Role.USER)
        }
    }
}
