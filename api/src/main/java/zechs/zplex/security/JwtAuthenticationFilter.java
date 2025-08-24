package zechs.zplex.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import zechs.zplex.auth.exception.JwtTokenNotValid;
import zechs.zplex.auth.utils.JwtUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            final String username = jwtUtil.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.doesTokenPassPreliminaryChecks(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
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
