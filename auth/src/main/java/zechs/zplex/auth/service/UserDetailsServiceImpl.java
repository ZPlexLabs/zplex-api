package zechs.zplex.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zechs.zplex.auth.exception.UserDoesNotExist;
import zechs.zplex.auth.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            user = userService.getUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

        } catch (UserDoesNotExist userDoesNotExist) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        } catch (Exception e) {
            // something went wrong and reject the request with internal server error
            throw new InternalAuthenticationServiceException(e.getMessage());
        }

        List<GrantedAuthority> authorities = user.getCapabilitiesAsEnum()
                .stream()
                .map(capability -> new SimpleGrantedAuthority(String.valueOf(capability.getId())))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }
}
