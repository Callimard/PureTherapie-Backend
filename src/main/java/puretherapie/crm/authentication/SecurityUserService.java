package puretherapie.crm.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.UserRepository;

@Service
public class SecurityUserService implements UserDetailsService {

    private final UserRepository userRepository;

    public SecurityUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        if ((user = userRepository.findByUsername(username)) != null)
            return user;
        else
            throw new UsernameNotFoundException("User with the username: " + username + " does not exist");

    }

}
