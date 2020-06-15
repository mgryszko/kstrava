package com.grysz.kstrava.token

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid

object NotBlankError

data class AccessTokenFileName(val name: String) {
    init {
        require(name.isNotBlank()) { "access token file name must not be blank" }
    }

    companion object {
        fun create(name: String): Validated<NotBlankError, AccessTokenFileName> =
            if (name.isNotBlank()) AccessTokenFileName(name).valid() else NotBlankError.invalid()
    }
}

data class AccessToken(val token: String) {
    init {
        require(token.isNotBlank()) { "token must not be blank" }
    }
}