package puretherapie.crm.api.v1.person.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientRegistrationSuccessDTO;
import puretherapie.crm.api.v1.person.client.service.ClientRegistrationService;
import puretherapie.crm.authentication.SecurityUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static puretherapie.crm.api.v1.person.client.controller.ClientController.CLIENTS_URL;
import static puretherapie.crm.api.v1.person.client.controller.ClientController.PARAM_DOUBLOON_VERIFICATION;
import static util.RequestTool.basicAuthorization;
import static util.RequestTool.httpPostJson;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ClientController tests")
public class ClientControllerTest {

    // Constants.

    private static final String USERNAME = "boss";
    private static final String PASSWORD = "password";

    // Variables.

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationService mockClientRegistration;

    @MockBean
    private SecurityUserService mockSecurityUserService;

    @Mock
    private UserDetails mockUserDetails;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nested
    @DisplayName("Client registration access tests")
    class ClientRegistration {

        @Test
        @DisplayName("Test client registration with empty body send an 500 http response")
        void testClientRegistrationWithEmptyBody() throws Exception {
            mockMvc.perform(httpPostJson(CLIENTS_URL).param(PARAM_DOUBLOON_VERIFICATION, "true"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Test client registration without request param but with correct body (no already existing client) send an 200 http response")
        void testClientRegistrationWithoutRequestParamAndNoClientDoubloon() throws Exception {
            prepareRegistrationSuccess();
            mockMvc.perform(httpPostJson(CLIENTS_URL).content(bodyCorrectClientInfo())).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Test client registration with correct authorization")
        void testClientRegistrationWithCorrectAuthorization() throws Exception {
            prepareRegistrationSuccess();
            prepareUsernameFind();

            mockMvc.perform(httpPostJson(CLIENTS_URL).content(bodyCorrectClientInfo()).header("Authorization", basicAuthorization(USERNAME,
                                                                                                                                  PASSWORD)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Test client registration with not correct authorization")
        void testClientRegistrationWithNotCorrectAuthorization() throws Exception {
            prepareRegistrationSuccess();
            prepareUsernameNotFound();

            mockMvc.perform(httpPostJson(CLIENTS_URL).content(bodyCorrectClientInfo()).header("Authorization", basicAuthorization(USERNAME,
                                                                                                                                  PASSWORD)))
                    .andExpect(status().isUnauthorized());
        }

    }

    private void prepareRegistrationSuccess() {
        given(mockClientRegistration.clientRegistration(any(), anyBoolean())).willReturn(ClientRegistrationSuccessDTO.builder().build());
    }

    private void prepareUsernameFind() {
        given(mockSecurityUserService.loadUserByUsername(USERNAME)).willReturn(mockUserDetails);
        given(mockUserDetails.getPassword()).willReturn(PASSWORD);
    }

    private void prepareUsernameNotFound() {
        given(mockSecurityUserService.loadUserByUsername(USERNAME)).willReturn(null);
    }

    private String bodyCorrectClientInfo() throws JsonProcessingException {
        ClientDTO info = ClientDTO.builder()
                .photo("photo_path")
                .comment("a comment")
                .technicalComment("a technical comment")
                .firstName("Guillaume")
                .lastName("Rakotomalala")
                .email("g.r@hotmail.fr")
                .gender(true)
                .birthday(null)
                .phone("+33607271440")
                .idPersonOrigin(1).build();

        return MAPPER.writeValueAsString(info);
    }
}
