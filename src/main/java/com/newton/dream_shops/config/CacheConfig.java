package com.newton.dream_shops.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    @Value("${app.cache.ttl.products:600}")
    private int productCacheTtl;

    @Value("${app.cache.ttl.categories:1800}")
    private int categoryCacheTtl;

    @Value("${app.cache.ttl.users:300}")
    private int userCacheTtl;

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(redisObjectMapper());
        return serializer;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(productCacheTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CacheNames.PRODUCTS, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCT_BY_ID, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_CATEGORY, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_BRAND, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_NAME, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_CATEGORY_AND_BRAND, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_BRAND_AND_NAME, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCT_COUNT, defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.CATEGORIES, defaultConfig.entryTtl(Duration.ofSeconds(categoryCacheTtl)));
        cacheConfigurations.put(CacheNames.USERS, defaultConfig.entryTtl(Duration.ofSeconds(userCacheTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    public static class CacheNames {
        public static final String PRODUCTS = "products";
        public static final String PRODUCT_BY_ID = "product_by_id";
        public static final String PRODUCTS_BY_CATEGORY = "products_by_category";
        public static final String PRODUCTS_BY_BRAND = "products_by_brand";
        public static final String PRODUCTS_BY_NAME = "products_by_name";
        public static final String PRODUCTS_BY_CATEGORY_AND_BRAND = "products_by_category_and_brand";
        public static final String PRODUCTS_BY_BRAND_AND_NAME = "products_by_brand_and_name";
        public static final String PRODUCT_COUNT = "product_count";
        public static final String CATEGORIES = "categories";
        public static final String USERS = "users";
    }
}