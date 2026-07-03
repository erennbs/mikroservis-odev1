package com.etiya.productservice.config;

import com.etiya.productservice.services.dtos.responses.GetAllProductsResponse;
import com.etiya.productservice.services.dtos.responses.GetByIdProductResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Dagitik cache konfigurasyonu (Redis).
 *
 * <p>{@code @EnableCaching} Spring Cache soyutlamasini (@Cacheable / @CacheEvict) acar.
 * Iki ayri cache tanimlanir:</p>
 * <ul>
 *   <li>{@link #PRODUCTS_CACHE} ("products") - {@code getAll()} sonucu olan
 *       {@code List<GetAllProductsResponse>}.</li>
 *   <li>{@link #PRODUCT_CACHE} ("product") - {@code getById()} sonucu olan tekil
 *       {@code GetByIdProductResponse}.</li>
 * </ul>
 *
 * <p>Her cache, degerini kendi <b>somut tipine bagli</b> bir JSON serializer ile saklar.
 * Boylece polimorfik "default typing" (JSON'a "@class"/tip-id gomme) kullanilmaz;
 * bu da Jackson 3'te yaz/oku asimetrisinden kaynaklanan
 * {@code MismatchedInputException} hatalarini tamamen ortadan kaldirir. Saklanan
 * deger sade JSON'dur (orn. {@code {"id":1,"name":...}}) ve okunurken bilinen tipe
 * dogrudan deserialize edilir.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** getAll() sonucunun (urun listesi) tutuldugu cache. */
    public static final String PRODUCTS_CACHE = "products";

    /** getById() sonucunun (tekil urun) tutuldugu cache. */
    public static final String PRODUCT_CACHE = "product";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        TypeFactory typeFactory = TypeFactory.createDefaultInstance();
        JavaType productListType =
                typeFactory.constructCollectionType(List.class, GetAllProductsResponse.class);

        RedisCacheConfiguration productsConfig = baseConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(productListType)));

        RedisCacheConfiguration productConfig = baseConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(GetByIdProductResponse.class)));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(Map.of(
                        PRODUCTS_CACHE, productsConfig,
                        PRODUCT_CACHE, productConfig))
                .build();
    }

    /** Tum cache'ler icin ortak temel ayarlar: 10 dk TTL, null degerleri cache'leme. */
    private RedisCacheConfiguration baseConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();
    }
}
