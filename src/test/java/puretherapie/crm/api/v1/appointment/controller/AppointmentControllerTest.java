package puretherapie.crm.api.v1.appointment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentFailDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentResponseDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentSuccessDTO;
import puretherapie.crm.api.v1.appointment.service.TakeAppointmentService;
import puretherapie.crm.api.v1.historical.service.HistoricalCreationService;
import puretherapie.crm.authentication.SecurityUserService;
import puretherapie.crm.data.person.user.User;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.APPOINTMENTS_URL;
import static puretherapie.crm.data.person.user.Role.BOSS_ROLE;
import static puretherapie.crm.data.person.user.Role.MAMY_ROLE;
import static util.RequestTool.httpPostJson;
import static util.RequestTool.httpPostJsonWithAuthorization;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AppointmentController tests")
public class AppointmentControllerTest {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TakeAppointmentService mockAppointCreatService;

    @MockBean
    private HistoricalCreationService mockNotifCreatService;

    @MockBean
    private SecurityUserService mockSecurityUserService;

    @Mock
    private User mockUser;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Tests.

    @BeforeEach
    void setUp() {
        prepareAlwaysNotificationCreationSuccess();
    }

    @Nested
    @DisplayName("takeAnAppointment tests")
    class TakeAnAppointment {

        @Test
        @DisplayName("Test if takeAnAppoint returns 500 with no request body")
        void testWithNoRequestBody() throws Exception {
            mockMvc.perform(httpPostJson(APPOINTMENTS_URL)).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Test with correct body and success create appointment returns 200")
        void testWithSuccessAppointmentCreation() throws Exception {
            given(mockAppointCreatService.takeAppointment(anyInt(), anyInt(), anyInt(), any(), any())).willReturn(successAppointmentCreationRes());

            mockMvc.perform(httpPostJson(APPOINTMENTS_URL).content(correctBody())).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Test with correct body and fail create appointment returns 400")
        void testWithFailAppointmentCreation() throws Exception {
            given(mockAppointCreatService.takeAppointment(anyInt(), anyInt(), anyInt(), any(), any())).willReturn(failAppointmentCreationRes());

            mockMvc.perform(httpPostJson(APPOINTMENTS_URL).content(correctBody())).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Test if with correct authentication with overlap permission enable; returns 200")
        void testWithPermissionAuthentication() throws Exception {
            prepareUserSecurityService(BOSS_ROLE);
            given(mockAppointCreatService.takeAppointment(anyInt(), anyInt(), anyInt(), any(), any(), anyBoolean())).willReturn(
                    successAppointmentCreationRes());

            mockMvc.perform(httpPostJsonWithAuthorization(APPOINTMENTS_URL, USERNAME, PASSWORD).content(correctBody()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Test if with correct authentication without overlap permission enable, returns 200 without overlap")
        void testWithNoPermissionAuthentication() throws Exception {
            prepareUserSecurityService(MAMY_ROLE);
            given(mockAppointCreatService.takeAppointment(anyInt(), anyInt(), anyInt(), any(), any())).willReturn(successAppointmentCreationRes());

            mockMvc.perform(httpPostJsonWithAuthorization(APPOINTMENTS_URL, USERNAME, PASSWORD).content(correctBody()))
                    .andExpect(status().isOk());
        }
    }

    private TakeAppointmentResponseDTO failAppointmentCreationRes() {
        return TakeAppointmentFailDTO.builder().error("TA error").build();
    }

    private TakeAppointmentResponseDTO successAppointmentCreationRes() {
        return TakeAppointmentSuccessDTO.builder().success("TA success").build();
    }

    private String correctBody() throws JsonProcessingException {
        return MAPPER.writeValueAsString(TakeAppointmentDTO.builder()
                                                 .idClient(1)
                                                 .idTechnician(1)
                                                 .idAestheticCare(1)
                                                 .overlapAuthorized(true)
                                                 .build());
    }

    private void prepareAlwaysNotificationCreationSuccess() {
        given(mockNotifCreatService.createHistorical(anyString(), anyString(), anyString(), anyBoolean())).willReturn(true);
    }

    private void prepareUserSecurityService(String role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        given(mockSecurityUserService.loadUserByUsername(anyString())).willReturn(mockUser);
        given(mockUser.getUsername()).willReturn(USERNAME);
        given(mockUser.getPassword()).willReturn(PASSWORD);
        //noinspection unchecked
        given((Collection<GrantedAuthority>) mockUser.getAuthorities()).willReturn(authorities);
    }
}
