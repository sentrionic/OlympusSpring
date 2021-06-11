package xyz.olympusblog.repository.specification

import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.Predicate
import xyz.olympusblog.models.Article
import xyz.olympusblog.models.Tag
import xyz.olympusblog.models.User

object ArticlesSpecifications {
    fun queryArticles(tag: Tag?, author: User?, favoritedBy: User?): Specification<Article> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            tag?.let {
                val tagList = root.get<Collection<Tag>>("tagList")
                predicates.add(cb.isMember(tag, tagList))
            }

            author?.let {
                val user = root.get<String>("author")
                predicates.add(cb.equal(user, author))
            }

            favoritedBy?.let {
                val favorited = root.get<Collection<User>>("favorites")
                predicates.add(cb.isMember(favoritedBy, favorited))
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    fun searchArticle(search: String?): Specification<Article> {
        val queryString = "%${search?.toLowerCase()?: ""}%"
        return Specification { root, _, cb ->
            cb.or(cb.like(root.get("description"), queryString), cb.like(root.get("title"), queryString))
        }
    }
}