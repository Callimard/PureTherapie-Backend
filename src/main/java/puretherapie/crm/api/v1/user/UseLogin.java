package puretherapie.crm.api.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.person.user.data.UserRepository;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.user.UseLogin.API_V1_USER_URL;

@RestController
@RequestMapping(API_V1_USER_URL)
public class UseLogin {

    public static final String API_V1_USER_URL = "/api/v1/user";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";

    @PostMapping(USER_LOGIN)
    public ResponseEntity<String> login() {
        String successLogin = """
                    {
                        "message":"Login success"
                    }
                """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(successLogin);
    }

    @PostMapping(USER_LOGOUT)
    public ResponseEntity<String> logout(HttpSession session) {
        if (session != null)
            session.invalidate();

        String successLogin = """
                    {
                        "message":"logout success"
                    }
                """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(successLogin);
    }
}
