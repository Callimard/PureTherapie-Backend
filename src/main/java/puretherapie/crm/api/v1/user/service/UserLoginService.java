package puretherapie.crm.api.v1.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;

@Slf4j
@Service
public class UserLoginService {

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
}
