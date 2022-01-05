package puretherapie.crm.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    // Variables.

    private final SecurityUserService securityUserService;

    // Constructors.

    public UserAuthenticationProvider(SecurityUserService securityUserService) {
        this.securityUserService = securityUserService;
    }

    // Methods.

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails user;
        try {
            user = securityUserService.loadUserByUsername(username);
            if (user != null) {
                if (!user.getPassword().equals(password))
                    throw new BadCredentialsException("Authentication failed for " + username);

                return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials().toString(),
                                                               user.getAuthorities());
            } else
                throw new UsernameNotFoundException("No found user for username: " + username);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Authentication failed for " + username);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
