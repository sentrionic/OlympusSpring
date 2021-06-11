package xyz.olympusblog.models

data class Email(
    val subject: String,
    val recipient: String,
    val body: String
)
