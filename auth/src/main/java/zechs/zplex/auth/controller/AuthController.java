package zechs.zplex.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zechs.zplex.auth.exception.*;
import zechs.zplex.auth.model.RefreshToken;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.api.*;
import zechs.zplex.auth.service.LoginRateLimiter;
import zechs.zplex.auth.service.TokenService;
import zechs.zplex.auth.service.UserService;
import zechs.zplex.auth.utils.PasswordUtil;
import zechs.zplex.common.model.ErrorResponse;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private final UserService userService;
    private final TokenService tokenService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(UserService userService, TokenService tokenService, LoginRateLimiter loginRateLimiter) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get access token for rest of the APIs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid password",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "429", description = "Too many login attempts",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String clientId = resolveClientIp(request);
        if (!loginRateLimiter.isAllowed(clientId)) {
            long retryAfter = loginRateLimiter.retryAfterSeconds(clientId);
            LOGGER.warning("Login rate limit exceeded for client: " + clientId);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Too many login attempts. Try again later."));
        }
        try {
            User user = userService.getUserByUsername(loginRequest.username());
            if (user == null) {
                LOGGER.warning("User not found: " + loginRequest.username());
                throw new UserDoesNotExist(loginRequest.username());
            }

            if (PasswordUtil.matches(loginRequest.password(), user.getPassword())) {
                if (PasswordUtil.needsRehash(user.getPassword())) {
                    userService.upgradePasswordHash(user, loginRequest.password());
                }
                loginRateLimiter.reset(clientId);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(tokenService.createRefreshToken(user));
            } else {
                loginRateLimiter.recordFailure(clientId);
                LOGGER.warning("Invalid password attempt for username: " + loginRequest.username());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorResponse("Invalid password"));
            }
        } catch (UserDoesNotExist notExist) {
            loginRateLimiter.recordFailure(clientId);
            LOGGER.warning("UserDoesNotExist exception: " + notExist.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "Signup new users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Bad signup form"),
            @ApiResponse(responseCode = "409", description = "Duplicate username"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            userService.createNewUser(signupRequest);
            LOGGER.info("User created successfully: " + signupRequest.username());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsernameConflict conflict) {
            LOGGER.warning("UsernameConflict exception: " + signupRequest.username());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during signup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{username}/capabilities")
    @Operation(summary = "Update user capabilities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Capabilities updated"),
            @ApiResponse(responseCode = "400", description = "Some unknown capability in request"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> updateUser(@PathVariable("username") String username,
                                        @Valid @RequestBody UpdateCapabilityRequest updateCapabilityRequest) {
        try {
            userService.updateUserCapabilities(username, updateCapabilityRequest.capabilities());
            LOGGER.info("Capabilities updated for username: " + username);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (UnknownCapability unknownCapability) {
            LOGGER.warning("UnknownCapability exception for username: " + username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserDoesNotExist notExist) {
            LOGGER.warning("UserDoesNotExist exception for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during updating capabilities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/admin/users/{username}")
    @Operation(summary = "Delete a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> deleteUser(@PathVariable("username") String username) {
        try {
            if ("admin".equalsIgnoreCase(username)) {
                LOGGER.warning("Attempt to delete admin user: " + username);
                throw new AdminDeletionNotAllowedException();
            }
            userService.deleteUser(username);
            LOGGER.info("User deleted successfully: " + username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (AdminDeletionNotAllowedException e) {
            LOGGER.warning("AdminDeletionNotAllowed exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (UserDoesNotExist notExist) {
            LOGGER.warning("UserDoesNotExist exception for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/admin/users")
    @Operation(summary = "List all users with capabilities and access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users listed"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> listUsers() {
        try {
            return ResponseEntity.ok(userService.listUsers());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during listing users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{username}/access")
    @Operation(summary = "Update user library and rating access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Access updated"),
            @ApiResponse(responseCode = "400", description = "Invalid access request"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> updateAccess(@PathVariable("username") String username,
                                          @Valid @RequestBody UserAccessRequest accessRequest) {
        try {
            userService.updateUserAccess(username, accessRequest.allowedLibraries(),
                    accessRequest.maxRatingRank(), accessRequest.allowUnrated());
            LOGGER.info("Access updated for username: " + username);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (InvalidAccessRequest invalid) {
            LOGGER.warning("InvalidAccessRequest for username " + username + ": " + invalid.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(invalid.getMessage()));
        } catch (UserDoesNotExist notExist) {
            LOGGER.warning("UserDoesNotExist exception for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during updating access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/admin/users/{username}/blacklist")
    @Operation(summary = "Add a title to a user's blacklist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Title blacklisted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> addBlacklist(@PathVariable("username") String username,
                                          @Valid @RequestBody BlacklistRequest blacklistRequest) {
        try {
            userService.addToBlacklist(username, blacklistRequest.mediaType(), blacklistRequest.tmdbId());
            LOGGER.info("Title blacklisted for username: " + username);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserDoesNotExist notExist) {
            LOGGER.warning("UserDoesNotExist exception for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during blacklisting title", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/admin/users/{username}/blacklist/{mediaType}/{tmdbId}")
    @Operation(summary = "Remove a title from a user's blacklist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Title removed from blacklist"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> removeBlacklist(@PathVariable("username") String username,
                                             @PathVariable("mediaType") zechs.zplex.common.model.MediaType mediaType,
                                             @PathVariable("tmdbId") int tmdbId) {
        try {
            userService.removeFromBlacklist(username, mediaType, tmdbId);
            LOGGER.info("Title removed from blacklist for username: " + username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserDoesNotExist notExist) {
            LOGGER.warning("UserDoesNotExist exception for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during removing blacklist title", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Use a valid refresh token to obtain a new access token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refreshed access token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenRefreshResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Refresh token expired or invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Refresh token not found for user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenRefreshRequest.class)
                    )
            )
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        String requestRefreshToken = request.refreshToken();
        try {
            Optional<RefreshToken> findToken = tokenService.findToken(requestRefreshToken);
            if (findToken.isPresent()) {
                LOGGER.info("Refresh token found, verifying expiration");
                // if its expired, it will throw exception
                tokenService.verifyExpiration(findToken.get());

                // generate new access token
                User user = findToken.get().getUser();
                LOGGER.info("Access token refreshed for user: " + user.getUsername());
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(tokenService.createAccessToken(user));
            } else {
                LOGGER.warning("Refresh token not found");
                throw new RefreshTokenNotFoundException();
            }
        } catch (RefreshTokenNotFoundException e) {
            LOGGER.warning("RefreshTokenNotFound exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ExpiredRefreshToken e) {
            LOGGER.warning("ExpiredRefreshToken exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Refresh token expired. Please login again."));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
