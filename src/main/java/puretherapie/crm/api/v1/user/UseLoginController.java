package puretherapie.crm.api.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;
import static puretherapie.crm.api.v1.user.UseLoginController.API_V1_USER_URL;

@RestController
@RequestMapping(API_V1_USER_URL)
public class UseLoginController {

    public static final String API_V1_USER_URL = "/api/v1/user";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";

    @PostMapping(USER_LOGIN)
    public ResponseEntity<String> login() {
        return generateOkJsonResponse("""
                                            {
                                                "message":"Login success"
                                            }
                                        """);
    }

    @PostMapping(USER_LOGOUT)
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
