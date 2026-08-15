package zechs.zplex.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import zechs.zplex.auth.exception.JwtTokenNotValid;
import zechs.zplex.auth.model.AuthenticatedUser;
import zechs.zplex.auth.model.TokenType;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.service.TokenRevocationService;
import zechs.zplex.auth.utils.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenRevocationService tokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final AuthenticatedUser authenticatedUser = jwtUtil.extractUser(jwt);
            if (authenticatedUser.tokenType() == TokenType.REFRESH) {
                logger.log(Level.WARNING, "Refresh token used in Authorization header. This is not allowed.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            User user = authenticatedUser.user();

            String jti = jwtUtil.extractJti(jwt);
            if (tokenRevocationService.isAccessJtiRevoked(jti)) {
                logger.log(Level.INFO, "Access token has been revoked");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (user != null) {
                Integer cachedVersion = tokenRevocationService.getCachedTokenVersion(user.getUsername());
                if (cachedVersion != null && cachedVersion != user.getTokenVersion()) {
                    logger.log(Level.INFO, "Access token version is stale");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (user != null && authentication == null) {

                if (jwtUtil.doesTokenPassPreliminaryChecks(jwt)) {
                    List<GrantedAuthority> authorities = user.getCapabilitiesAsEnum()
                            .stream()
                            .map(capability -> new SimpleGrantedAuthority(String.valueOf(capability.getId())))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null, authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.log(Level.INFO, "JWT token did not pass preliminary check");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (JwtTokenNotValid | UsernameNotFoundException exception) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
