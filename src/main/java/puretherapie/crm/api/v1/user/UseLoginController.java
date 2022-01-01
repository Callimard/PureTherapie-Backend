package puretherapie.crm.api.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;
import static puretherapie.crm.api.v1.user.UseLoginController.API_V1_USER_URL;

@RestController
@RequestMapping(API_V1_USER_URL)
public class UseLoginController {

    public static final String API_V1_USER_URL = "/api/v1/users";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";

    @PostMapping(USER_LOGIN)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> login() {
        return generateOkJsonResponse("""
                                            {
                                                "message":"Login success"
                                            }
                                        """);
    }

    @PostMapping(USER_LOGOUT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout(HttpSession session) {
        invalidateSession(session);
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
