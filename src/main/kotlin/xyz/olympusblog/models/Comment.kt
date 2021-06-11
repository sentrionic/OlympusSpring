package xyz.olympusblog.models

import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.NotEmpty

@Entity
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @NotEmpty
    @Max(150)
    val body: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId", referencedColumnName = "id")
    val article: Article,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", referencedColumnName = "id")
    val author: User,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
