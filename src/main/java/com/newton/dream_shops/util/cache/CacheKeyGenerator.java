package com.newton.dream_shops.util.cache;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CacheKeyGenerator {

    private static final String SEPARATOR = ":";
    private static final String NULL_VALUE = "null";

    /**
     * Generate a cache key by joining multiple parts with a separator
     * 
     * @param parts The parts to join
     * @return The generated cache key
     */
    public String generateKey(String... parts) {
        return Arrays.stream(parts)
                .map(part -> part != null ? part : NULL_VALUE)
                .collect(Collectors.joining(SEPARATOR));
    }

    /**
     * Generate a cache key by joining multiple objects with a separator
     * 
     * @param parts The objects to join (will be converted to strings)
     * @return The generated cache key
     */
    public String generateKey(Object... parts) {
        return Arrays.stream(parts)
                .map(part -> part != null ? part.toString() : NULL_VALUE)
                .collect(Collectors.joining(SEPARATOR));
    }

    /**
     * Generate a product-specific cache key with a type prefix
     * 
     * @param type   The product operation type (e.g., "by_category", "by_brand")
     * @param params Additional parameters for the key
     * @return The generated product cache key
     */
    public String generateProductKey(String type, Object... params) {
        StringBuilder key = new StringBuilder("product").append(SEPARATOR).append(type);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a category-specific cache key with a type prefix
     * 
     * @param type   The category operation type
     * @param params Additional parameters for the key
     * @return The generated category cache key
     */
    public String generateCategoryKey(String type, Object... params) {
        StringBuilder key = new StringBuilder("category").append(SEPARATOR).append(type);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }


    /**
     * Generate a compound key for operations involving multiple parameters
     * 
     * @param prefix The key prefix
     * @param params The parameters to include in the key
     * @return The generated compound key
     */
    public String generateCompoundKey(String prefix, Object... params) {
        if (params == null || params.length == 0) {
            return prefix;
        }

        StringBuilder key = new StringBuilder(prefix);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a simple key for single parameter operations
     * 
     * @param parameter The single parameter
     * @return The parameter as a string key
     */
    public String generateSimpleKey(Object parameter) {
        return parameter != null ? parameter.toString() : NULL_VALUE;
    }
}