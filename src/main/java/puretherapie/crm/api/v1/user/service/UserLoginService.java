package puretherapie.crm.api.v1.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import puretherapie.crm.ForgottenPasswordConfiguration;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.UserRepository;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;

@Slf4j
@AllArgsConstructor
@Service
public class UserLoginService {

    // Variables.

    private final ForgottenPasswordConfiguration forgottenPasswordConfiguration;

    private final UserRepository userRepository;
    private final JavaMailSender emailSender;

    // Methods.

    public ResponseEntity<String> successLogin(Authentication authentication) {
        log.info("Success login of the user {}", authentication.getName());
        return generateOkJsonResponse("""
                                                  {
                                                      "message":"Login success"
                                                  }
                                              """);
    }

    public ResponseEntity<String> successLogout(HttpSession session, Authentication authentication) {
        invalidateSession(session);
        log.info("Success logout of the user {}", authentication.getName());
        return generateOkJsonResponse("""
                                                  {
                                                      "message":"logout success"
                                                  }
                                              """);
    }

    private void invalidateSession(HttpSession session) {
        if (session != null)
            session.invalidate();
    }

    public void userForgotPassword(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            String newPassword = genPassword();
            changeUserPassword(user, newPassword);
            SimpleMailMessage newPasswordMessage = generateNewPasswordMailMessage(user.getUsername(), newPassword);
            emailSender.send(newPasswordMessage);
        } else
            throw new UserException("Unknown username");
    }

    private void changeUserPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepository.save(user);
        log.info("Update password for the user => {}", user.getUsername());
    }

    private SimpleMailMessage generateNewPasswordMailMessage(String username, String newPassword) {
        String mailText = """
                Bonjour,
                Voici le nouveau mot de passe pour l'utilisateur %s:
                %s
                 """.formatted(username, newPassword);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(forgottenPasswordConfiguration.getSendFrom());
        message.setTo(forgottenPasswordConfiguration.getSendTo());
        message.setSubject("Nouveau mot de passe pour l'utilisateur " + username);
        message.setText(mailText);

        return message;
    }

    private String genPassword() {
        PasswordGenerator gen = new PasswordGenerator();

        CharacterData alphabeticalChars = EnglishCharacterData.Alphabetical;
        CharacterRule alphabeticalCaseRule = new CharacterRule(alphabeticalChars);
        alphabeticalCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return DigestDictionaryRule.ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(17, splCharRule, alphabeticalCaseRule, digitRule);
    }

    // Exception.

    public static class UserException extends RuntimeException {
        public UserException(String message) {
            super(message);
        }
    }
}
