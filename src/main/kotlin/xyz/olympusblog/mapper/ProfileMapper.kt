package xyz.olympusblog.mapper

import org.springframework.stereotype.Service
import xyz.olympusblog.models.User
import xyz.olympusblog.repository.UserRepository
import xyz.olympusblog.response.Profile

@Service
class ProfileMapper(private val userRepository: UserRepository) {
    fun mapUserToProfile(user: User, currentUser: User? = null): Profile {
        return user.run {
            Profile(
                id = id.toInt(),
                username = username,
                bio = bio,
                image = image,
                following = if (currentUser != null) user.followers.contains(currentUser) else false,
                followers = user.followers.count(),
                followee = userRepository.getFolloweeCount(id.toInt())
            )
        }
    }
}