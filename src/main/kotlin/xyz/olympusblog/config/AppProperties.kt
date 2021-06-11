package xyz.olympusblog.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {

    @Value("\${spring.redis.host}")
    lateinit var redisUrl: String

    @Value("\${spring.redis.port}")
    lateinit var redisPort: String

    @Value("\${aws.accessKey}")
    lateinit var awsAccessKey: String

    @Value("\${aws.secretAccessKey}")
    lateinit var awsSecretAccessKey: String

    @Value("\${aws.storageBucketName}")
    lateinit var awsStorageBucketName: String

    @Value("\${aws.region}")
    lateinit var awsRegion: String

    @Value("\${spring.mail.username}")
    lateinit var gmailUser: String

    @Value("\${spring.mail.password}")
    lateinit var gmailPassword: String
}