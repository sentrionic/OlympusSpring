package xyz.olympusblog.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Entity
@Table(name="users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0,

    @Column(unique = true)
    @field:NotBlank(message = "Username is required")
    var username: String,

    @field:Email
    @Column(unique = true)
    @field:NotEmpty(message = "Email is required")
    var email: String,

    @field:NotBlank(message = "Password is required")
    @JsonIgnore
    var password: String,

    @DefaultValue("")
    var bio: String,

    var image: String,

    @ManyToMany
    @JoinTable(name = "user_followings")
    @JsonIgnore
    val followers: MutableList<User> = mutableListOf(),

    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
)