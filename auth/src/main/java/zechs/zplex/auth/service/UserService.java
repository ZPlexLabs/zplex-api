package zechs.zplex.auth.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.auth.exception.*;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.api.SignupRequest;
import zechs.zplex.auth.repository.UserRepository;
import zechs.zplex.auth.utils.PasswordUtil;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.common.capability.Capability;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            admin.setPassword(PasswordUtil.hashPassword(envAdminPassword));
            admin.setCapabilities(new int[]{1, 2, 3, 4, 5});
            admin.setAdult(true);

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
            boolean updatePassword = !admin.getPassword().equals(PasswordUtil.hashPassword(envAdminPassword));

            if (updateCapabilities || updatePassword) {
                if (updateCapabilities) admin.setCapabilities(supportedCapabilityIds);
                if (updatePassword) admin.setPassword(PasswordUtil.hashPassword(envAdminPassword));

                userRepository.save(admin);

                logger.log(Level.INFO, "Admin user updated with new " +
                        (updateCapabilities ? "capabilities" : "") +
                        (updateCapabilities && updatePassword ? " and " : "") +
                        (updatePassword ? "password" : "") + ".");
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
            newUser.setPassword(PasswordUtil.hashPassword(signupRequest.password()));
            newUser.setCapabilities(new int[]{});
            newUser.setAdult(false);

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
            logger.log(Level.INFO, "Deleted user: " + username);
        } catch (UserDoesNotExist ex) {
            throw ex;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete user " + username + ": " + e.getMessage(), e);
            throw new UserUpdateFailed("Failed to delete user " + username + ": " + e.getMessage());
        }
    }
}
