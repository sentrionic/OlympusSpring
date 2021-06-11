package xyz.olympusblog.controller

import org.springframework.web.bind.annotation.*
import xyz.olympusblog.response.Profile
import xyz.olympusblog.service.ProfileService

@RestController
@RequestMapping("/api/profiles")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping
    fun getProfiles(@RequestParam(required = false) search: String?): List<Profile> {
        return profileService.getProfiles(search)
    }

    @GetMapping("/{username}")
    fun getProfileByUsername(@PathVariable username: String): Profile {
        return profileService.getProfileByUsername(username)
    }

    @PostMapping("/{username}/follow")
    fun followProfile(@PathVariable username: String): Profile {
        return profileService.followProfile(username)
    }

    @DeleteMapping("/{username}/follow")
    fun unfollowProfile(@PathVariable username: String): Profile {
        return profileService.unfollowProfile(username)
    }
}