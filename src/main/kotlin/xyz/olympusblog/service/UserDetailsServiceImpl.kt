package xyz.olympusblog.service

import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import xyz.olympusblog.repository.UserRepository
import javax.transaction.Transactional

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {

    @Transactional
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email).takeIf { it != null }?: throw UsernameNotFoundException("No user found with email $email")
        return SpringUser(user.username, user.password,
            true, true, true,
            true, getAuthorities("USER"));
    }

    private fun getAuthorities(role: String): Collection<GrantedAuthority?> {
        return listOf(SimpleGrantedAuthority(role))
    }
}