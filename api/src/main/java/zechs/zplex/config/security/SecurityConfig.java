package zechs.zplex.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public access
                        .requestMatchers(Endpoints.SWAGGER).permitAll()
                        .requestMatchers(Endpoints.PUBLIC).permitAll()

                        // Token Api
                        .requestMatchers(HttpMethod.POST, Endpoints.AUTH)
                        .permitAll()

                        // Logout (any authenticated user)
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout", "/api/auth/logout/all")
                        .authenticated()

                        // Admin endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.PUT, "/api/auth/admin/users/{username}/capabilities")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.GET, "/api/auth/admin/users")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.PUT, "/api/auth/admin/users/{username}/access")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.POST, "/api/auth/admin/users/{username}/blacklist")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.DELETE, "/api/auth/admin/users/{username}/blacklist/{mediaType}/{tmdbId}")
                        .hasAuthority(Capabilities.UPDATE_USERS_CAPABILITIES.getIdAsString())

                        .requestMatchers(HttpMethod.DELETE, "/api/auth/admin/users/{username}")
                        .hasAuthority(Capabilities.DELETE_USERS.getIdAsString())

                        // View content
                        .requestMatchers(HttpMethod.GET, Endpoints.VIEW)
                        .hasAuthority(Capabilities.VIEW.getIdAsString())

                        // Capabilities endpoints
                        .requestMatchers(HttpMethod.GET, "/api/config/capabilities")
                        .hasAnyAuthority(Capabilities.getAllCapabilities().stream()
                                .map(cap -> String.valueOf(cap.getId()))
                                .toArray(String[]::new))

                        // Config endpoint
                        .requestMatchers(HttpMethod.GET, "/api/config")
                        .hasAnyAuthority(
                                Capabilities.VIEW.getIdAsString(),
                                Capabilities.STREAM.getIdAsString(),
                                Capabilities.DOWNLOAD.getIdAsString(),
                                Capabilities.MANAGE_CONTENT.getIdAsString()
                        )

                        // Personal watch state (all methods require VIEW)
                        .requestMatchers("/api/me/**")
                        .hasAuthority(Capabilities.VIEW.getIdAsString())

                        // Deny everything else
                        .anyRequest().denyAll()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${zplex.cors.allowed-origins:}") String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
