package ru.vizbash.cloudsend.data

interface ConnectionParamsRepository {
    val isInitialized: Boolean

    fun save(params: ConnectionParams)

    fun get(): ConnectionParams
}
