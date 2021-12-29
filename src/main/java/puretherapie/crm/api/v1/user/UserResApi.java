package puretherapie.crm.api.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.person.user.data.UserRepository;

import static puretherapie.crm.api.v1.user.UserResApi.API_V1_USER_URL;

@RestController
@RequestMapping(API_V1_USER_URL)
public class UserResApi {

    public static final String API_V1_USER_URL = "/api/v1/user";
    public static final String USER_LOGIN = "/login";

    private final UserRepository userRepository;

    public UserResApi(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping(USER_LOGIN)
    public ResponseEntity<String> tryLogin() {
        String successLogin = """
                    {
                        "message":"Login success"
                    }
                """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(successLogin);
    }
}
