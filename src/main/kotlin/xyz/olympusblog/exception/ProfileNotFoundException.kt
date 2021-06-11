package xyz.olympusblog.exception

import javassist.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Profile not found")
class ProfileNotFoundException(message: String): NotFoundException(message)