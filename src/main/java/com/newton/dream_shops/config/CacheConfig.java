package com.newton.dream_shops.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }


    @Bean("cacheValueSerializer")
    public GenericJackson2JsonRedisSerializer cacheValueSerializer() {
        return new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(cacheValueSerializer());
        template.setHashValueSerializer(cacheValueSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(productCacheTtl))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(cacheValueSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Product caches
        cacheConfigurations.put(CacheNames.PRODUCTS,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCT_BY_ID,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_CATEGORY,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_BRAND,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_NAME,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_CATEGORY_AND_BRAND,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCTS_BY_BRAND_AND_NAME,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));
        cacheConfigurations.put(CacheNames.PRODUCT_COUNT,
                defaultConfig.entryTtl(Duration.ofSeconds(productCacheTtl)));

        // Category caches
        cacheConfigurations.put(CacheNames.CATEGORIES,
                defaultConfig.entryTtl(Duration.ofSeconds(categoryCacheTtl)));

        // User caches
        cacheConfigurations.put(CacheNames.USERS,
                defaultConfig.entryTtl(Duration.ofSeconds(userCacheTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
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

    public static class CacheKeyGenerator {
        public static String generateKey(String... parts) {
            return String.join(":", parts);
        }

        public static String generateProductKey(String type, Object... params) {
            StringBuilder key = new StringBuilder("product:").append(type);
            for (Object param : params) {
                key.append(":").append(param != null ? param.toString() : "null");
            }
            return key.toString();
        }
    }
}