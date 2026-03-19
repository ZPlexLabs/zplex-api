package zechs.zplex.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPooled jedis() {
        return new JedisPooled(RedisUrlBuilder.buildRedisUrl());
    }

}