package com.fankit.common.exception

abstract class BusinessException(
    val code: String,
    override val message: String,
) : RuntimeException(message)

class NotFoundException(code: String, message: String) : BusinessException(code, message)
class ConflictException(code: String, message: String) : BusinessException(code, message)
class UnauthorizedException(code: String, message: String) : BusinessException(code, message)
class InvalidRequestException(code: String, message: String) : BusinessException(code, message)
