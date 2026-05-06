package com.fankit.user.infrastructure.persistence

import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.User
import com.fankit.user.domain.port.outbound.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {

    override fun save(user: User): User =
        jpaRepository.save(UserJpaEntity.fromDomain(user)).toDomain()

    override fun findByEmail(email: Email): User? =
        jpaRepository.findByEmail(email.value)?.toDomain()

    override fun findById(id: Long): User? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun existsByEmail(email: Email): Boolean =
        jpaRepository.existsByEmail(email.value)
}
