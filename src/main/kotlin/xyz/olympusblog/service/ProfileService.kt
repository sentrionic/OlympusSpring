package xyz.olympusblog.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import xyz.olympusblog.exception.ProfileNotFoundException
import xyz.olympusblog.mapper.ProfileMapper
import xyz.olympusblog.models.User
import xyz.olympusblog.repository.UserRepository
import xyz.olympusblog.response.Profile

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val profileMapper: ProfileMapper
) {

    fun getProfiles(search: String?): List<Profile> {
        val currentUser = getAuthenticatedUser()
        val users = userRepository.findProfiles(search ?: "")
        return users.map { profile -> profileMapper.mapUserToProfile(profile, currentUser) }
    }

    fun getProfileByUsername(username: String): Profile {
        val currentUser = getAuthenticatedUser()
        val profile = userRepository.findByUsername(username)
            ?: throw ProfileNotFoundException("A profile for $username does not exist")
        return profileMapper.mapUserToProfile(profile, currentUser)
    }

    fun followProfile(username: String): Profile {
        val profile = userRepository.findByUsername(username)
            ?: throw ProfileNotFoundException("A profile for $username does not exist")

        val currentUser = getAuthenticatedUser()
            ?: throw UsernameNotFoundException("Current user could not be found")

        if (!profile.followers.contains(currentUser)) {
            profile.followers.add(currentUser)
            userRepository.save(profile)
        }

        return profileMapper.mapUserToProfile(profile, currentUser)
    }

    fun unfollowProfile(username: String): Profile {
        val profile = userRepository.findByUsername(username)
            ?: throw ProfileNotFoundException("A profile for $username does not exist")

        val currentUser = getAuthenticatedUser()
            ?: throw UsernameNotFoundException("Current user could not be found")

        if (profile.followers.contains(currentUser)) {
            profile.followers.remove(currentUser)
            userRepository.save(profile)
        }

        return profileMapper.mapUserToProfile(profile, currentUser)
    }

    private fun getAuthenticatedUser(): User? {
        return userRepository.findByUsername(SecurityContextHolder.getContext().authentication.name)
    }

}