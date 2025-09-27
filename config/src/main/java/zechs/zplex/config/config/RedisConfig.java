package zechs.zplex.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPooled jedis() {
        return new JedisPooled(
                System.getenv("REDIS_HOST"),
                Integer.parseInt(System.getenv("REDIS_PORT")),
                System.getenv("REDIS_USERNAME"),
                System.getenv("REDIS_PASSWORD")
        );
    }

}