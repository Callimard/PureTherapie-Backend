package puretherapie.crm.api.v1.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.user.service.UserLoginService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;

import javax.servlet.http.HttpSession;

import static puretherapie.crm.api.v1.user.controller.UserController.API_V1_USER_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(API_V1_USER_URL)
public class UserController {

    // Constants.

    public static final String API_V1_USER_URL = "/api/v1/users";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGIN_URL = API_V1_USER_URL + USER_LOGIN;
    public static final String USER_LOGOUT = "/logout";
    public static final String USER_LOGOUT_URL = API_V1_USER_URL + USER_LOGOUT;

    public static final String USER_FORGET_PASSWORD = "/passwordForgotten";
    public static final String USER_FORGET_PASSWORD_URL = API_V1_USER_URL + USER_FORGET_PASSWORD;

    // Variables.

    private final UserLoginService userLoginService;

    // Methods.

    @PostMapping(USER_FORGET_PASSWORD)
    public void forgotPassword(@RequestParam(name = "username") String username) {
        userLoginService.userForgotPassword(username);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(USER_LOGIN)
    public ResponseEntity<String> login(Authentication authentication) {
        return userLoginService.successLogin(authentication);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(USER_LOGOUT)
    public ResponseEntity<String> logout(HttpSession session, Authentication authentication) {
        return userLoginService.successLogout(session, authentication);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(path = USER_LOGIN, method = RequestMethod.HEAD)
    public ResponseEntity<SimpleResponseDTO> checkLogin() {
        return SimpleResponseDTO.generateResponse(SimpleResponseDTO.generateSuccess("Success check login"));
    }
}
