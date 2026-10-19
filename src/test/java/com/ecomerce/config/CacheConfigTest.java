package com.ecomerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void cacheManagerExposesTheProductsCache() {
        CacheManager manager = new CacheConfig().cacheManager();

        assertNotNull(manager.getCache("products"));
        assertTrue(manager.getCacheNames().contains("products"));
    }
}
