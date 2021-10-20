package no.nav.syfo.application.cache

import no.nav.syfo.util.configuredJacksonMapper
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.exceptions.JedisConnectionException

class RedisStore(
    private val jedisPool: JedisPool,
) {
    val objectMapper = configuredJacksonMapper()

    inline fun <reified T> getObject(
        key: String,
    ): T? {
        return get(key)?.let { it ->
            objectMapper.readValue(it, T::class.java)
        }
    }

    fun get(
        key: String,
    ): String? {
        try {
            jedisPool.resource.use { jedis ->
                return jedis.get(key)
            }
        } catch (e: JedisConnectionException) {
            log.warn("Got connection error when fetching from redis! Continuing without cached value", e)
            return null
        }
    }

    fun <T> setObject(
        expireSeconds: Long,
        key: String,
        value: T,
    ) {
        val valueJson = objectMapper.writeValueAsString(value)
        set(
            expireSeconds = expireSeconds,
            key = key,
            value = valueJson,
        )
    }

    fun set(
        expireSeconds: Long,
        key: String,
        value: String,
    ) {
        try {
            jedisPool.resource.use { jedis ->
                jedis.setex(
                    key,
                    expireSeconds,
                    value,
                )
            }
        } catch (e: JedisConnectionException) {
            log.warn("Got connection error when storing in redis! Continue without caching", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RedisStore::class.java)
    }
}
