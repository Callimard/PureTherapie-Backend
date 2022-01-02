package puretherapie.crm.api.v1.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;
import static puretherapie.crm.api.v1.user.UseLoginController.API_V1_USER_URL;

@Slf4j
@RestController
@RequestMapping(API_V1_USER_URL)
public class UseLoginController {

    public static final String API_V1_USER_URL = "/api/v1/users";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";

    @PostMapping(USER_LOGIN)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> login(Authentication authentication) {
        log.info("Success login of the user {}", authentication.getName());
        return generateOkJsonResponse("""
                                                  {
                                                      "message":"Login success"
                                                  }
                                              """);
    }

    @PostMapping(USER_LOGOUT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout(HttpSession session, Authentication authentication) {
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
