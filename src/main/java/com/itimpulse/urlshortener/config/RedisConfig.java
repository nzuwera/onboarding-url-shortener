package com.itimpulse.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  /**
   * Configures the connection factory for Redis.
   * This bean establishes the connection to the Redis server using Lettuce.
   *
   * @return The configured Redis connection factory.
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(redisHost);
    redisConfig.setPort(redisPort);

    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
    factory.afterPropertiesSet();
    return factory;
  }

  /**
   * Configures the RedisTemplate for interacting with Redis.
   * This template is customized to use JSON serialization for storing objects,
   * which allows for storing complex objects like ShortenUrl.
   *
   * @return The configured RedisTemplate.
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());

    // Use String serializer for keys.
    template.setKeySerializer(new StringRedisSerializer());
    
    // Configure ObjectMapper with JavaTimeModule to correctly serialize Java 8 time objects.
    // This is crucial for handling the 'ttl' field in the ShortenUrl model.
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
    
    // Use JSON serializer for values and hash values.
    template.setValueSerializer(jsonSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonSerializer);
    return template;
  }

  /**
   * Configures the cache manager for Spring's caching abstraction.
   * This sets a default time-to-live (TTL) for cache entries and disables caching of null values.
   *
   * @return The configured CacheManager.
   */
  @Bean
  public CacheManager cacheManager() {
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            // Set a default expiration time for cache entries.
            .entryTtl(Duration.ofMinutes(30))
            // Do not cache null values, to prevent issues with missing data.
            .disableCachingNullValues();

    return RedisCacheManager.builder(redisConnectionFactory()).cacheDefaults(config).build();
  }
}
