package puretherapie.crm.api.v1.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import puretherapie.crm.ForgottenPasswordConfiguration;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.UserRepository;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@AllArgsConstructor
@Service
public class UserPasswordService {

    // Variables.

    private final Random random = new SecureRandom();
    private final Map<Long, String> mapCodeUser = new HashMap<>();

    private final ForgottenPasswordConfiguration forgottenPasswordConfiguration;

    private final JavaMailSender emailSender;

    private final UserRepository userRepository;

    // Methods.

    public void userForgotPassword(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            long urlCode = generatePasswordReInitUrlCode(username);
            SimpleMailMessage newPasswordMessage = generateInitPasswordMail(user.getUsername(), urlCode);
            emailSender.send(newPasswordMessage);
        } else
            throw new UserLoginService.UserException("Unknown username");
    }

    private SimpleMailMessage generateInitPasswordMail(String username, long urlCode) {
        String mailText = """
                Bonjour,
                Voici le lien pour définir un nouveau mot de passe pour l'utilisateur %s:
                %s
                 """.formatted(username,
                               forgottenPasswordConfiguration.getFrontResetPasswordUrl() + "?code=" + urlCode + "&username=" + username);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(forgottenPasswordConfiguration.getSendFrom());
        message.setTo(forgottenPasswordConfiguration.getSendTo());
        message.setSubject("Ré initialiser mot de passe pour l'utilisateur  " + username);
        message.setText(mailText);

        return message;
    }

    /**
     * Generate an url code for the specified username and store it.
     *
     * @param username the username
     *
     * @return the url code generated
     */
    public long generatePasswordReInitUrlCode(String username) {
        long urlCode = random.nextLong(Long.MAX_VALUE);
        while (mapCodeUser.containsKey(urlCode))
            urlCode = random.nextLong(Long.MAX_VALUE);
        mapCodeUser.put(urlCode, username);
        return urlCode;
    }

    public void resetPassword(long urlCode, String username, String password) {
        String associatedUsername = mapCodeUser.get(urlCode);
        if (associatedUsername != null) {
            if (associatedUsername.equals(username)) {
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    resetPassword(password, user);
                    mapCodeUser.remove(urlCode);
                } else
                    throw new UserPasswordException("No user for the username " + username);
            } else
                throw new UserPasswordException("Url code not associated to the specified username " + username);
        } else
            throw new UserPasswordException("Unknown url code");
    }

    private void resetPassword(String password, User user) {
        user.setPassword(password);
        user = userRepository.save(user);
        log.info("Reset password for the user => {}", user);
    }

    // Exceptions.

    public static class UserPasswordException extends RuntimeException {
        public UserPasswordException(String message) {
            super(message);
        }
    }
}
