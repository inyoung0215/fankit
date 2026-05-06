package com.fankit.user.infrastructure.persistence

import com.fankit.common.entity.BaseEntity
import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.EncodedPassword
import com.fankit.user.domain.model.Role
import com.fankit.user.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "users",
    indexes = [Index(name = "idx_users_email", columnList = "email", unique = true)],
)
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(name = "password", nullable = false, length = 100)
    var password: String,

    @Column(nullable = false, length = 50)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role,
) : BaseEntity() {

    fun toDomain(): User = User(
        id = id,
        email = Email(email),
        password = EncodedPassword(password),
        nickname = nickname,
        role = role,
    )

    companion object {
        fun fromDomain(user: User): UserJpaEntity = UserJpaEntity(
            id = user.id,
            email = user.email.value,
            password = user.password.value,
            nickname = user.nickname,
            role = user.role,
        )
    }
}
