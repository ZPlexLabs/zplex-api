package zechs.zplex.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.UUID;
import org.springframework.stereotype.Component;
import zechs.zplex.auth.exception.JwtTokenNotValid;
import zechs.zplex.auth.model.AuthenticatedUser;
import zechs.zplex.auth.model.TokenType;
import zechs.zplex.auth.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtUtil {

    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());
    private static final int ACCESS_TOKEN_VALIDATE_IN_HOURS = 24; // 1 day
    private static final int REFRESH_TOKEN_VALIDATE_IN_HOURS = 24 * 365; // 1 year
    private static final String TOKEN_ISSUER = "zplex-api";
    private static final int MIN_KEY_LENGTH = 32;
    private final SecretKey jwtKey;

    public JwtUtil() {
        String secretKey = System.getenv("SECRET_KEY");

        if (secretKey == null || secretKey.length() < MIN_KEY_LENGTH) {
            throw new RuntimeException("The provided secret key is too short. It must be at least "
                    + MIN_KEY_LENGTH + " characters (256 bits) for HS256.");
        }

        jwtKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public AuthenticatedUser extractUser(String token) throws JwtTokenNotValid {
        try {
            Claims claims = extractAllClaims(token);
            User user = new User();
            user.setFirstName(claims.get("firstName", String.class));
            user.setLastName(claims.get("lastName", String.class));
            user.setUsername(claims.get("username", String.class));
            user.setAdult(claims.get("isAdult", Boolean.class));
            int[] capabilities = ((List<?>) claims.get("capabilities"))
                    .stream()
                    .mapToInt(o -> ((Number) o).intValue())
                    .toArray();
            user.setCapabilities(capabilities);
            String tokenTypeStr = claims.get("tokenType", String.class);
            TokenType tokenType = TokenType.valueOf(tokenTypeStr.toUpperCase(Locale.ENGLISH));
            logger.log(Level.INFO, "Successfully extracted jwt for user " + user.getUsername() + " with " + capabilities.length + " capabilities.");
            return new AuthenticatedUser(user, tokenType);
        } catch (JwtException | NullPointerException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Something went wrong in method extractUser(" + token + ")", e);
            throw new JwtTokenNotValid();
        }
    }

    public String extractUsername(String token) throws JwtTokenNotValid {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public String extractJti(String token) throws JwtTokenNotValid {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    private String extractIssuer(String token) throws JwtTokenNotValid {
        return extractClaim(token, Claims::getIssuer);
    }

    public Date extractExpiration(String token) throws JwtTokenNotValid {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtTokenNotValid {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws JwtTokenNotValid {
        try {
            return Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.log(Level.SEVERE, "Something went wrong in method extractAllClaims(" + token + ")", e);
            throw new JwtTokenNotValid();
        }
    }

    public Boolean doesTokenPassPreliminaryChecks(String token) throws JwtTokenNotValid {
        return !isTokenExpired(token) && isTokenIssuerValid(token);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Boolean isTokenIssuerValid(String token) throws JwtTokenNotValid {
        return Objects.equals(extractIssuer(token), TOKEN_ISSUER);
    }

    public String generateRefreshToken(User user) {
        return createToken(user, REFRESH_TOKEN_VALIDATE_IN_HOURS, TokenType.REFRESH);
    }

    public String generateAccessToken(User user) {
        return createToken(user, ACCESS_TOKEN_VALIDATE_IN_HOURS, TokenType.ACCESS);
    }

    private String createToken(User user, int duration, TokenType tokenType) {
        long currentTimeMillis = System.currentTimeMillis();

        return Jwts.builder()
                .claim("jti", UUID.randomUUID().toString())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("username", user.getUsername())
                .claim("capabilities", user.getCapabilities())
                .claim("isAdult", user.getAdult())
                .claim("tokenType", tokenType.name().toLowerCase(Locale.ENGLISH))
                .issuer(TOKEN_ISSUER)
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + convertToHours(duration)))
                .signWith(jwtKey, Jwts.SIG.HS256)
                .compact();
    }

    private long convertToHours(int hours) {
        return hours * 60L * 60L * 1000L;
    }


}
