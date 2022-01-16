package puretherapie.crm.api.v1.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.user.service.UserLoginService;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.user.controller.UserController.API_V1_USER_URL;

@Slf4j
@AllArgsConstructor
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*", origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST,
                                                                                                            RequestMethod.OPTIONS})
@RestController
@RequestMapping(API_V1_USER_URL)
public class UserController {

    // Constants.

    public static final String API_V1_USER_URL = "/api/v1/users";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";

    // Variables.

    private final UserLoginService userLoginService;

    // Methods.

    @PostMapping(USER_LOGIN)
    public ResponseEntity<String> login(Authentication authentication) {
        return userLoginService.successLogin(authentication);
    }

    @PostMapping(USER_LOGOUT)
    public ResponseEntity<String> logout(HttpSession session, Authentication authentication) {
        return userLoginService.successLogout(session, authentication);
    }
}
