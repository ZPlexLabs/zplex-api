package zechs.zplex.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.security.JwtAuthenticationFilter;

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
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public access
                        .requestMatchers(Endpoints.SWAGGER).permitAll()
                        .requestMatchers(Endpoints.PUBLIC).permitAll()

                        // Token Api
                        .requestMatchers(HttpMethod.POST, Endpoints.AUTH)
                        .hasAnyAuthority(Capabilities.getAllCapabilitiesId())

                        // Admin endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/auth/admin/users/{username}/capabilities")
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

                        // Deny everything else
                        .anyRequest().denyAll()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
