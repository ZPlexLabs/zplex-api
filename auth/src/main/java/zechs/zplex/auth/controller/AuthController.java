package zechs.zplex.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zechs.zplex.auth.exception.*;
import zechs.zplex.auth.model.RefreshToken;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.api.*;
import zechs.zplex.auth.service.TokenService;
import zechs.zplex.auth.service.UserService;
import zechs.zplex.auth.utils.PasswordUtil;
import zechs.zplex.common.model.ErrorResponse;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
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
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.getUserByUsername(loginRequest.username());
            if (user == null) {
                throw new UserDoesNotExist(loginRequest.username());
            }

            if (user.getPassword().equals(PasswordUtil.hashPassword(loginRequest.password()))) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(tokenService.createRefreshToken(user));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorResponse("Invalid password"));
            }
        } catch (UserDoesNotExist notExist) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
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
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            if (signupRequest.password().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorResponse("Password must be at least 8 characters"));
            }
            userService.createNewUser(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsernameConflict conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{username}/capabilities")
    @Operation(summary = "Signup new users")
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
                                        @RequestBody UpdateCapabilityRequest updateCapabilityRequest) {
        try {
            userService.updateUserCapabilities(username, updateCapabilityRequest.capabilities());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (UnknownCapability unknownCapability) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserDoesNotExist notExist) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
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
            userService.deleteUser(username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserDoesNotExist notExist) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
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
            @RequestBody TokenRefreshRequest request
    ) {
        String requestRefreshToken = request.refreshToken();
        try {
            Optional<RefreshToken> findToken = tokenService.findByToken(requestRefreshToken);
            if (findToken.isPresent()) {
                // if its expired, it will throw exception
                tokenService.verifyExpiration(findToken.get());

                // generate new access token
                User user = findToken.get().getUser();
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(tokenService.createAccessToken(user));
            } else {
                throw new RefreshTokenNotFoundException();
            }
        } catch (RefreshTokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ExpiredRefreshToken e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Refresh token expired. Please login again."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}
