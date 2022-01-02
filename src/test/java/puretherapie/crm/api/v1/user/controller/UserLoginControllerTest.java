package puretherapie.crm.api.v1.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import puretherapie.crm.authentication.SecurityUserService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static puretherapie.crm.api.v1.user.controller.UseLoginController.*;
import static util.RequestTool.JSON_CONTENT_TYPE;
import static util.TestTool.btoaCredentials;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserLoginController tests")
class UserLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityUserService mockUserService;

    @Mock
    private UserDetails mockUserDetails;

    private static final String USERNAME_0 = "username_0";

    private static final String PASSWORD_0 = "password_0";
    private static final String PASSWORD_1 = "password_1";


    @Nested
    @DisplayName("User login tests")
    class UserLogin {

        @Test
        @DisplayName("Test user login with no Authorization header")
        void testUserLoginWithoutAuthorization() throws Exception {
            mockMvc.perform(post(API_V1_USER_URL + USER_LOGIN)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user login with unknown username")
        void testUserLoginWithUnknownUsername() throws Exception {
            String authorization = prepareUnknownUsernameContext();

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGIN)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user login with wrong username/password combination")
        void testUserLoginWithWrongUsernamePasswordCombination() throws Exception {
            String authorization = prepareCorrectPassword0Context(PASSWORD_1);

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGIN)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user login with correct credentials in Authorization header")
        void testUserLoginWithCorrectCredential() throws Exception {
            String authorization = prepareCorrectPassword0Context(PASSWORD_0);

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGIN)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isOk());
        }

    }

    @Nested
    @DisplayName("User logout tests")
    class UserLogout {

        @Test
        @DisplayName("Test user logout with no Authorization header")
        void testUserLogoutWithNotAuthorizationHeader() throws Exception {
            mockMvc.perform(post(API_V1_USER_URL + USER_LOGOUT)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user logout with unknown username")
        void testUserLogoutWithUnknownUsername() throws Exception {
            String authorization = prepareUnknownUsernameContext();

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGOUT)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user logout with wrong user/password combination")
        void testUserLogoutWithWrongUsernamePasswordCombination() throws Exception {
            String authorization = prepareCorrectPassword0Context(PASSWORD_1);

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGOUT)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Test user logout with correct credentials")
        void testUserLogoutWithCorrectCredentials() throws Exception {
            String authorization = prepareCorrectPassword0Context(PASSWORD_0);

            mockMvc.perform(post(API_V1_USER_URL + USER_LOGOUT)
                                    .header("Authorization", authorization)
                                    .contentType(JSON_CONTENT_TYPE)).andExpect(status().isOk());
        }
    }

    private String prepareUnknownUsernameContext() {
        String authorization = "Basic " + btoaCredentials(USERNAME_0, PASSWORD_0);

        given(mockUserService.loadUserByUsername(USERNAME_0)).willThrow(UsernameNotFoundException.class);
        return authorization;
    }

    private String prepareCorrectPassword0Context(String password) {
        String authorization = "Basic " + btoaCredentials(USERNAME_0, password);

        given(mockUserService.loadUserByUsername(USERNAME_0)).willReturn(mockUserDetails);
        given(mockUserDetails.getPassword()).willReturn(PASSWORD_0);
        return authorization;
    }
}
