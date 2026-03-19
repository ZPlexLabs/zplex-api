package zechs.zplex.config.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RedisUrlBuilder {

    public static String buildRedisUrl() {
        String host = System.getenv("REDIS_HOST");

        int port;
        try {
            port = Integer.parseInt(System.getenv("REDIS_PORT"));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("REDIS_PORT must be a valid integer");
        }

        String username = System.getenv("REDIS_USERNAME");
        String password = System.getenv("REDIS_PASSWORD");

        String encodedUsername = urlEncode(username);
        String encodedPassword = urlEncode(password);

        return String.format(
                "rediss://%s:%s@%s:%d",
                encodedUsername,
                encodedPassword,
                host,
                port
        );
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}