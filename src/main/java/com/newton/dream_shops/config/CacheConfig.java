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
import com.newton.dream_shops.constants.CacheConstants;
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

        Map<String, RedisCacheConfiguration> cacheConfigurations = createCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    private Map<String, RedisCacheConfiguration> createCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Product caches
        addProductCacheConfigurations(cacheConfigurations, defaultConfig);
        
        // Category caches
        addCategoryCacheConfigurations(cacheConfigurations, defaultConfig);
        
        // User caches
        addUserCacheConfigurations(cacheConfigurations, defaultConfig);

        return cacheConfigurations;
    }

    private void addProductCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations, 
                                             RedisCacheConfiguration defaultConfig) {
        Duration productTtl = Duration.ofSeconds(productCacheTtl);
        
        cacheConfigurations.put(CacheConstants.PRODUCTS, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCT_BY_ID, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCTS_BY_CATEGORY, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCTS_BY_BRAND, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCTS_BY_NAME, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCTS_BY_CATEGORY_AND_BRAND, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCTS_BY_BRAND_AND_NAME, defaultConfig.entryTtl(productTtl));
        cacheConfigurations.put(CacheConstants.PRODUCT_COUNT, defaultConfig.entryTtl(productTtl));
    }

    private void addCategoryCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations, 
                                              RedisCacheConfiguration defaultConfig) {
        Duration categoryTtl = Duration.ofSeconds(categoryCacheTtl);
        cacheConfigurations.put(CacheConstants.CATEGORIES, defaultConfig.entryTtl(categoryTtl));
    }

    private void addUserCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations, 
                                          RedisCacheConfiguration defaultConfig) {
        Duration userTtl = Duration.ofSeconds(userCacheTtl);
        cacheConfigurations.put(CacheConstants.USERS, defaultConfig.entryTtl(userTtl));
    }
}