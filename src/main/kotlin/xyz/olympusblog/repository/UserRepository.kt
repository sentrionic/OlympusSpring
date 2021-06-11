package xyz.olympusblog.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import xyz.olympusblog.models.User
import xyz.olympusblog.response.Profile

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByEmail(email: String): User?
    fun findById(id: Int): User?
    fun findByEmailAndPassword(email: String, password: String): User?
    fun findByUsername(username: String): User?
    @Query(value = "SELECT u FROM User u WHERE u.username LIKE %:search% OR u.bio LIKE %:search%")
    fun findProfiles(@Param("search") search: String): List<User>

    @Query(nativeQuery = true, value = "SELECT count(user_id) FROM user_followings where followers_id = :id")
    fun getFolloweeCount(@Param("id") id: Int): Int
}