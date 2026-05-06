package com.fankit.user.domain.port.outbound

import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.User

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: Email): User?
    fun findById(id: Long): User?
    fun existsByEmail(email: Email): Boolean
}
