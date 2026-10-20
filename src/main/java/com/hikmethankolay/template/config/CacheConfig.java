package com.hikmethankolay.template.config;

import com.hikmethankolay.template.user.dto.UserResponse;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.json.JsonMapper;

/**
 * Redis-backed Spring Cache.
 *
 * <ul>
 *   <li>Values are stored as JSON with a fixed type per cache, never with Java serialization or
 *       polymorphic type information, so cached data cannot be used for deserialization attacks.</li>
 *   <li>TTL and key prefix come from {@code spring.cache.redis.*} in {@code application.yml}.</li>
 *   <li>The cache manager is transaction-aware: puts and evictions inside a transaction are applied
 *       only after it commits, so a rolled-back update never leaves stale data behind.</li>
 *   <li>Writes are immediate: each put and eviction has completed in Redis before the call returns.
 *       With Lettuce, Spring Data Redis otherwise sends them asynchronously, so a cache fill from a read
 *       could land after a later eviction and keep a deleted or changed user cached until the TTL ends.</li>
 *   <li>Cache errors are logged and ignored, so a Redis outage slows the API down instead of breaking it.</li>
 * </ul>
 *
 * <p>To cache something new, add a name to {@link CacheNames}, register its value type in
 * {@link #redisCacheCustomizer(JsonMapper, RedisConnectionFactory)} and annotate the service method with
 * {@code @Cacheable} / {@code @CacheEvict}. Cache DTOs, not JPA entities.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /**
     * Configures the value serializer of each cache, immediate writes and transaction awareness.
     *
     * @param jsonMapper        the application's JSON mapper, so cached values match API serialization
     * @param connectionFactory the Redis connection the cache writer uses
     * @return the customizer applied by Spring Boot's Redis cache auto-configuration
     */
    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheCustomizer(
            JsonMapper jsonMapper, RedisConnectionFactory connectionFactory) {
        return builder -> builder.cacheWriter(
                        RedisCacheWriter.create(connectionFactory, writer -> writer.immediateWrites()))
                .transactionAware()
                .withCacheConfiguration(
                        CacheNames.USERS,
                        builder.cacheDefaults()
                                .serializeValuesWith(SerializationPair.fromSerializer(
                                        new JacksonJsonRedisSerializer<>(jsonMapper, UserResponse.class))));
    }

    /**
     * Logs cache failures (e.g. Redis unavailable) instead of failing the request;
     * the annotated method then runs as if there were no cache.
     *
     * @return the error handler used by all caches
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    /** Names of the application's caches. */
    public static final class CacheNames {

        /** {@code UserResponse} by user id. */
        public static final String USERS = "users";

        /** Constants only. */
        private CacheNames() {}
    }
}
