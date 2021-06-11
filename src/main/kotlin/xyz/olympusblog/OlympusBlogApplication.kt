package xyz.olympusblog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableAsync
import xyz.olympusblog.config.SwaggerConfiguration

@SpringBootApplication
@Import(SwaggerConfiguration::class)
@EnableAsync
class OlympusBlogApplication

fun main(args: Array<String>) {
	runApplication<OlympusBlogApplication>(*args)
}
