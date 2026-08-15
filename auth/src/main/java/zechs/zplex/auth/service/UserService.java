package zechs.zplex.auth.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.auth.exception.*;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.UserBlacklist;
import zechs.zplex.auth.model.UserBlacklistId;
import zechs.zplex.auth.model.api.BlacklistEntry;
import zechs.zplex.auth.model.api.SignupRequest;
import zechs.zplex.auth.model.api.UserSummaryResponse;
import zechs.zplex.auth.repository.UserBlacklistRepository;
import zechs.zplex.auth.repository.UserRepository;
import zechs.zplex.auth.utils.PasswordUtil;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.common.capability.Capability;
import zechs.zplex.common.model.Library;
import zechs.zplex.common.model.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final UserAccessService userAccessService;

    @Autowired
    public UserService(UserRepository userRepository, UserBlacklistRepository userBlacklistRepository,
                       UserAccessService userAccessService) {
        this.userRepository = userRepository;
        this.userBlacklistRepository = userBlacklistRepository;
        this.userAccessService = userAccessService;
    }

    @Transactional
    public void createAdminUser(String envAdminPassword) {
        try {
            if (envAdminPassword == null || envAdminPassword.length() < 8) {
                throw new RuntimeException("Admin password should be at least 8 characters long.");
            }

            if (userRepository.existsByUsername("admin")) {
                logger.log(Level.INFO, "Admin user already exists. Updating...");
                updateAdminUser(envAdminPassword);
                return;
            }

            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("Administrator");
            admin.setLastName("");
            admin.setPassword(PasswordUtil.encode(envAdminPassword));
            admin.setCapabilities(new int[]{1, 2, 3, 4, 5});
            admin.setAdult(true);
            admin.setAllowedLibraries(Library.getAllIds());
            admin.setMaxRatingRank(Integer.MAX_VALUE); // no rating ceiling
            admin.setAllowUnrated(true);

            userRepository.save(admin);
            logger.log(Level.INFO, "Created admin user successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create admin user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create admin user: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateAdminUser(String envAdminPassword) {
        try {
            User admin = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Admin user does not exist"));

            int[] supportedCapabilityIds = Capabilities.getAllCapabilities().stream()
                    .mapToInt(Capability::getId)
                    .toArray();

            boolean updateCapabilities = !Arrays.equals(admin.getCapabilities(), supportedCapabilityIds);
            boolean updatePassword = !PasswordUtil.matches(envAdminPassword, admin.getPassword())
                    || PasswordUtil.needsRehash(admin.getPassword());
            boolean updateAccess = !Arrays.equals(admin.getAllowedLibraries(), Library.getAllIds())
                    || admin.getMaxRatingRank() != Integer.MAX_VALUE
                    || !admin.isAllowUnrated();

            if (updateCapabilities || updatePassword || updateAccess) {
                if (updateCapabilities) admin.setCapabilities(supportedCapabilityIds);
                if (updatePassword) admin.setPassword(PasswordUtil.encode(envAdminPassword));
                if (updateAccess) {
                    admin.setAllowedLibraries(Library.getAllIds());
                    admin.setMaxRatingRank(Integer.MAX_VALUE); // no rating ceiling
                    admin.setAllowUnrated(true);
                }

                userRepository.save(admin);

                logger.log(Level.INFO, "Admin user updated.");
            } else {
                logger.log(Level.INFO, "Admin user is up-to-date.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update admin user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update admin user: " + e.getMessage(), e);
        }
    }

    public User getUserByUsername(String username) throws UserDoesNotExist {
        try {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.log(Level.WARNING, "User " + username + " does not exist.");
                        return new UserDoesNotExist(username);
                    });
        } catch (UserDoesNotExist ex) {
            throw ex;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get user " + username + ": " + e.getMessage(), e);
            return null;
        }
    }

    // Re-hashes a verified legacy password to BCrypt so old SHA-256 hashes are phased out on login.
    @Transactional
    public void upgradePasswordHash(User user, String rawPassword) {
        user.setPassword(PasswordUtil.encode(rawPassword));
        userRepository.save(user);
        logger.log(Level.INFO, "Upgraded password hash to BCrypt for user " + user.getUsername());
    }

    @Transactional
    public void createNewUser(SignupRequest signupRequest) throws UsernameConflict {
        try {
            if (userRepository.existsByUsername(signupRequest.username())) {
                logger.log(Level.INFO, "User " + signupRequest.username() + " already exists.");
                throw new UsernameConflict(signupRequest.username());
            }

            User newUser = new User();
            newUser.setUsername(signupRequest.username());
            newUser.setFirstName(signupRequest.firstName());
            newUser.setLastName(signupRequest.lastName());
            newUser.setPassword(PasswordUtil.encode(signupRequest.password()));
            newUser.setCapabilities(new int[]{});
            newUser.setAdult(false);
            newUser.setAllowedLibraries(new int[]{}); // no library access until admin grants
            newUser.setMaxRatingRank(0);
            newUser.setAllowUnrated(false);

            userRepository.save(newUser);
            logger.log(Level.INFO, "User " + signupRequest.username() + " created.");
        } catch (UsernameConflict conflict) {
            throw conflict;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create user " + signupRequest.username() + ": " + e.getMessage(), e);
            throw new UserNotCreated("User not created: " + e.getMessage());
        }
    }

    @Transactional
    public void updateUserCapabilities(String username, int[] capabilities)
            throws UserDoesNotExist, UnknownCapability {
        try {
            validateCapabilities(capabilities);
            User user = getUserByUsername(username);
            user.setCapabilities(capabilities);
            userRepository.save(user);
            logger.log(Level.INFO, "Updated capabilities for user: " + username);
        } catch (UserDoesNotExist ex) {
            throw ex;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update capabilities for user " + username + ": " + e.getMessage(), e);
            throw new UserUpdateFailed("Failed to update capabilities for user " + username + ": " + e.getMessage());
        }
    }

    private void validateCapabilities(int[] capabilities) throws UnknownCapability {
        Set<Integer> validCapabilities = Capabilities.getAllCapabilities().stream()
                .map(Capability::getId)
                .collect(Collectors.toSet());

        for (int capability : capabilities) {
            if (!validCapabilities.contains(capability)) {
                throw new UnknownCapability(String.valueOf(capability));
            }
        }
    }

    @Transactional
    public void deleteUser(String username) throws UserDoesNotExist {
        try {
            User user = getUserByUsername(username);
            userRepository.delete(user);
            userAccessService.evict(username);
            logger.log(Level.INFO, "Deleted user: " + username);
        } catch (UserDoesNotExist ex) {
            throw ex;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete user " + username + ": " + e.getMessage(), e);
            throw new UserUpdateFailed("Failed to delete user " + username + ": " + e.getMessage());
        }
    }

    public List<UserSummaryResponse> listUsers() {
        List<UserSummaryResponse> summaries = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            summaries.add(toSummary(user));
        }
        return summaries;
    }

    private UserSummaryResponse toSummary(User user) {
        List<BlacklistEntry> blacklist = userBlacklistRepository.findByIdUsername(user.getUsername()).stream()
                .map(entry -> new BlacklistEntry(entry.getId().getMediaType(), entry.getId().getTmdbId()))
                .collect(Collectors.toList());
        return new UserSummaryResponse(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getCapabilities(), user.getAdult(), user.getAllowedLibraries(),
                user.getMaxRatingRank(), user.isAllowUnrated(), blacklist);
    }

    @Transactional
    public void updateUserAccess(String username, int[] allowedLibraries, int maxRatingRank, boolean allowUnrated)
            throws UserDoesNotExist, InvalidAccessRequest {
        validateLibraries(allowedLibraries);
        User user = getUserByUsername(username);
        user.setAllowedLibraries(allowedLibraries);
        user.setMaxRatingRank(maxRatingRank);
        user.setAllowUnrated(allowUnrated);
        userRepository.save(user);
        userAccessService.evict(username);
        logger.log(Level.INFO, "Updated access for user: " + username);
    }

    private void validateLibraries(int[] allowedLibraries) throws InvalidAccessRequest {
        for (int id : allowedLibraries) {
            if (Library.getById(id) == null) {
                throw new InvalidAccessRequest("Unknown library id: " + id);
            }
        }
    }

    @Transactional
    public void addToBlacklist(String username, MediaType mediaType, int tmdbId) throws UserDoesNotExist {
        getUserByUsername(username);
        userBlacklistRepository.save(new UserBlacklist(username, mediaType, tmdbId));
        userAccessService.evict(username);
        logger.log(Level.INFO, "Blacklisted " + mediaType + " " + tmdbId + " for user: " + username);
    }

    @Transactional
    public void removeFromBlacklist(String username, MediaType mediaType, int tmdbId) throws UserDoesNotExist {
        getUserByUsername(username);
        userBlacklistRepository.deleteById(new UserBlacklistId(username, mediaType, tmdbId));
        userAccessService.evict(username);
        logger.log(Level.INFO, "Removed blacklist " + mediaType + " " + tmdbId + " for user: " + username);
    }
}
