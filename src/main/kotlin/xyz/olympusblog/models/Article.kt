package xyz.olympusblog.models

import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Entity
@Table(name="articles")
data class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @NotBlank(message = "Title cannot be empty or Null")
    @Min(3)
    @Max(100)
    var title: String,

    @NotBlank(message = "Description cannot be empty or Null")
    @Min(3)
    @Max(150)
    var description: String,

    @NotBlank(message = "Body cannot be empty or Null")
    @Lob
    var body: String,

    var slug: String,

    var image: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", referencedColumnName = "id")
    val author: User,

    @ManyToMany
    @JoinTable(name = "article_favorites")
    val favorites: MutableList<User> = mutableListOf(),

    @ManyToMany
    @JoinTable(name = "article_bookmarks")
    val bookmarks: MutableList<User> = mutableListOf(),

    @ManyToMany
    var tagList: MutableList<Tag> = mutableListOf(),

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    @PreRemove
    fun cleanLists() {
        favorites.clear()
        bookmarks.clear()
        tagList.clear()
    }
}