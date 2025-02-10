package no.nav.syfo.application.cache

import no.nav.syfo.util.configuredJacksonMapper
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.exceptions.JedisConnectionException

class ValkeyStore(
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

    inline fun <reified T> getListObject(key: String): List<T>? {
        val value = get(key)
        return if (value != null) {
            objectMapper.readValue(
                value,
                objectMapper.typeFactory.constructCollectionType(ArrayList::class.java, T::class.java)
            )
        } else {
            null
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
            log.warn("Got connection error when fetching from valkey! Continuing without cached value", e)
            return null
        }
    }

    fun <T> setObject(
        expireSeconds: Long,
        key: String,
        value: T,
    ) {
        val valueJson = objectMapper.writeValueAsString(value)
        if (expireSeconds > 0) {
            set(
                expireSeconds = expireSeconds,
                key = key,
                value = valueJson,
            )
        } else {
            val message = "Won't put value into the valkey-cache with expireSeconds=$expireSeconds"
            log.warn(message, Exception(message))
        }
    }

    private fun set(
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
            log.warn("Got connection error when storing in valkey! Continue without caching", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ValkeyStore::class.java)
    }
}
