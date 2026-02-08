package ru.vizbash.cloudsend.data.persistence.settings

//enum class SettingKey(val value: String) {
//    AutoAcceptTransfers("auto_accept"),
//    SaveDirectoryUri("save_directory")
//}

sealed interface Setting<T> {
    val key: String
    val default: T

    data class Str(
        override val key: String,
        override val default: String?,
    ) : Setting<String?>

    data class Bool(
        override val key: String,
        override val default: Boolean,
    ) : Setting<Boolean>
}

object AppSettings {
    val SaveDirectoryUri = Setting.Str("save_directory", null)
    val AutoAcceptTransfers = Setting.Bool("auto_accept", false)
}

