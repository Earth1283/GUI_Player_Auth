package io.github.earth1283.guiplayerauth.config

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordUtils {
    
    // Cost of 10 is standard for modern apps
    fun hashPin(pin: String): String {
        return BCrypt.withDefaults().hashToString(10, pin.toCharArray())
    }

    fun verifyPin(pin: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(pin.toCharArray(), hash).verified
    }
}
