package io.github.earth1283.guiplayerauth.database

import java.util.UUID

interface Database {
    fun connect()
    fun close()
    fun isRegistered(uuid: UUID): Boolean
    fun getPlayerHash(uuid: UUID): String?
    fun registerPlayer(uuid: UUID, hash: String, ip: String)
    fun updateLogin(uuid: UUID, ip: String)
}
