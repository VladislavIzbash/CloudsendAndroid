package ru.vizbash.cloudsend.data.persistence

interface ConnectionParamsRepository {
    val isInitialized: Boolean

    fun save(params: ConnectionParams)

    fun get(): ConnectionParams
}
