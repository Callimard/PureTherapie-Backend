package puretherapie.crm.api.v1.client.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import puretherapie.crm.api.v1.client.ClientInformation;
import puretherapie.crm.api.v1.client.service.ClientRegistrationService;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static puretherapie.crm.api.v1.client.controller.ClientController.*;
import static puretherapie.crm.tool.ControllerTool.SUCCESS_FIELD;
import static util.RequestTool.httpPostJson;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ClientController tests")
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationService mockClientRegistration;

    @MockBean
    private ClientRepository mockClientRepository;

    @Mock
    private Client mockClient;

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Nested
    @DisplayName("Client registration tests")
    class ClientRegistration {

        @Test
        @DisplayName("Test client registration with empty body send an 500 http response")
        void testClientRegistrationWithEmptyBody() throws Exception {
            mockMvc.perform(httpPostJson(API_V1_CLIENT_URL).param(PARAM_DOUBLOON_VERIFICATION, "true"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Test client registration without request param but with correct body (no already existing client) send an 200 http response")
        void testClientRegistrationWithoutRequestParamAndNoClientDoubloon() throws Exception {
            prepareRegistrationSuccess();
            mockMvc.perform(httpPostJson(API_V1_CLIENT_URL).content(bodyCorrectClientInfo())).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Test client registration without request param but correct body with doubloon ind send an 400 http response")
        void testClientRegistrationWithoutRequestParamAndClientDoubloon() throws Exception {
            prepareRegistrationFindDoubloons();
            mockMvc.perform(httpPostJson(API_V1_CLIENT_URL).content(bodyCorrectClientInfo())).andExpect(status().isBadRequest());
        }

    }

    private void prepareRegistrationSuccess() {
        given(mockClientRegistration.clientRegistration(any(), anyBoolean())).willReturn(ResponseEntity.ok(
                Collections.singletonMap(SUCCESS_FIELD, "Client registration success")));
    }

    private void prepareRegistrationFindDoubloons() {
        List<ClientInformation> doubloon = new ArrayList<>();
        doubloon.add(ClientInformation.builder().build());
        given(mockClientRegistration.clientRegistration(any(), anyBoolean())).willReturn(ResponseEntity.status(
                HttpStatus.BAD_REQUEST).body(Collections.singletonMap(CLIENT_DOUBLOON_FIELD, doubloon)));
    }

    private static String bodyCorrectClientInfo() throws JsonProcessingException {
        ClientInformation info = ClientInformation.builder()
                .photo("photo_path")
                .comment("a comment")
                .technicalComment("a technical comment")
                .firstName("Guillaume")
                .lastName("Rakotomalala")
                .email("g.r@hotmail.fr")
                .gender(true)
                .birthday(null)
                .phone("+33607271440")
                .idOrigin(1).build();

        return MAPPER.writeValueAsString(info);
    }
}
