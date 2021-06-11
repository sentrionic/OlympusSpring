package xyz.olympusblog.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Tag(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    val name: String = "",
)