package com.fankit.admin.domain.port.outbound

interface UserAdminClient {
    fun promoteToCreator(userId: Long)
}
