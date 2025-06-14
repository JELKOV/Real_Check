package com.realcheck.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig
 *
 * - Redis 설정 클래스
 * - Redis 연결 및 RedisTemplate 설정을 담당
 * - Lettuce 기반 커넥션을 사용하며, 문자열 기반 직렬화 설정 적용
 */
@Configuration
@EnableCaching // Spring의 @Cacheable 등 캐싱 기능 활성화
public class RedisConfig {

    /**
     * Redis 연결 팩토리 설정
     *
     * - LettuceConnectionFactory를 통해 Redis 서버와 연결
     * - 기본 설정 (localhost:6379)을 사용하며, 필요 시 application.yml/properties에서 설정 가능
     *
     * @return RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * RedisTemplate 설정
     *
     * - RedisTemplate은 Redis에 데이터를 저장하고 조회할 때 사용하는 핵심 객체
     * - 문자열 기반의 Key/Value 저장을 위해 StringRedisSerializer 사용
     *
     * @param cf RedisConnectionFactory (Redis 연결 객체)
     * @return RedisTemplate<String, String>
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        // 키와 값 모두 문자열로 직렬화 (예: viewed:1:23 = "1")
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
