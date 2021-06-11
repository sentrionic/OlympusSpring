package xyz.olympusblog.service

import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import org.springframework.stereotype.Service
import xyz.olympusblog.config.AppProperties
import xyz.olympusblog.exception.BadTokenException
import java.util.*

@Service
class RedisService(private val appProperties: AppProperties) {
    private val redis = RedisClient.create("redis://${appProperties.redisUrl}:${appProperties.redisPort}").connect().sync()

    fun saveUserId(id: String, token: UUID) {
        redis.set("forget-password:${token}", id, SetArgs().ex(1000 * 60 * 60 * 24 * 3))
    }

    fun getUserId(token: String): Long {
        val key = "forget-password:${token}"
        val value = redis.get(key) ?: throw BadTokenException("Token Expired")
        return value.toLong()
    }

    fun deleteKey(token: String) {
        val key = "forget-password:${token}"
        redis.del(key)
    }
}