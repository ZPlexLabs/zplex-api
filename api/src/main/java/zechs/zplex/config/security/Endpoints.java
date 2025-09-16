package zechs.zplex.config.security;


public class Endpoints {
    public static final String[] SWAGGER = {
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] PUBLIC = {
            "/health",
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh"
    };

    public static final String[] AUTH = {
            "/api/auth/refresh"
    };

    public static final String[] VIEW = {
            "/api/movie/**",
            "/api/tvshows/**",
            "/api/suggestion",
            "/api/suggestion/search"
    };
}
