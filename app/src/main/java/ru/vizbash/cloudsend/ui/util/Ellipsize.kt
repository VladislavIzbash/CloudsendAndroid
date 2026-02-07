package ru.vizbash.cloudsend.ui.util

fun String.ellipsize(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.substring(0, maxLength + 1) + "…"
    }
}