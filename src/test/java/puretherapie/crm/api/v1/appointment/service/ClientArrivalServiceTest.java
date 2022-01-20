package puretherapie.crm.api.v1.appointment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.waitingroom.service.PlaceInWaitingRoomService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientArrival;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientArrivalRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.appointment.service.ClientArrivalService.*;

@SpringBootTest
@DisplayName("ClientArrivalService tests")
public class ClientArrivalServiceTest {

    @Autowired
    private ClientArrivalService cas;

    @Nested
    @DisplayName("ClientArrive tests")
    class ClientArrive {

        @Test
        @DisplayName("Test with not found client fail")
        void testWithNotFoundClient() {
            prepareClientRepository();
            Map<String, Object> res = cas.clientArrive(46);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with too much client delay client delay fail")
        void testWithNormalClientDelay() {
            prepareClientRepository();
            prepareFindAppointment();
            prepareTooMuchDelay();

            Map<String, Object> res = cas.clientArrive(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_TOO_MUCH_LATE_ERROR);
        }

        @Test
        @DisplayName("Test with fail to place in waiting room fail")
        void testWithFailToPlaceInWaitingRoom() {
            prepareClientRepository();
            prepareFindAppointment();
            prepareNotLate();
            prepareFailPlaceInWaitingRoom();

            Map<String, Object> res = cas.clientArrive(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, WAITING_ROOM_ERROR);
        }

        @Test
        @DisplayName("Test with all good success")
        void testWithAllGood() {
            prepareClientRepository();
            prepareFindAppointment();
            prepareNotLate();
            prepareSuccessPlaceInWaitingRoom();

            Map<String, Object> res = cas.clientArrive(CLIENT_ID);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with acceptable delay success")
        void testWithAcceptableDelay() {
            prepareClientRepository();
            prepareFindAppointment();
            prepareAcceptableDelay();
            prepareSuccessCreateClientDelay();
            prepareSuccessPlaceInWaitingRoom();

            Map<String, Object> res = cas.clientArrive(CLIENT_ID);
            verifySuccess(res);
        }

    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(cas.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(cas.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(cas.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 752;

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;
    private static final int APPOINTMENT_ID = 213;

    @MockBean
    private ClientArrivalRepository mockCARepository;
    @Mock
    private ClientArrival mockCA;

    @MockBean
    private ClientDelayService mockCDService;
    private static final LocalTime APPOINTMENT_TIME_NOT_LATE = LocalTime.now().plusMinutes(10);
    private static final LocalTime APPOINTMENT_ACCEPTABLE_DELAY = LocalTime.now().minusMinutes(ClientDelayService.getMaximumClientDelay() - 2);
    private static final LocalTime APPOINTMENT_TIME_TOO_MUCH_DELAY = LocalTime.now().minusMinutes(ClientDelayService.getMaximumClientDelay() + 2);

    @MockBean
    private PlaceInWaitingRoomService mockPCWRService;

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareFindAppointment() {
        given(mockAppointmentRepository.findByClientAndDay(eq(mockClient), any())).willReturn(mockAppointment);
    }

    private void prepareNotFoundAppointment() {
        given(mockAppointmentRepository.findByClientAndDay(any(), any())).willReturn(null);
    }

    private void prepareNotLate() {
        given(mockAppointment.getTime()).willReturn(APPOINTMENT_TIME_NOT_LATE);
    }

    private void prepareAcceptableDelay() {
        given(mockAppointment.getTime()).willReturn(APPOINTMENT_ACCEPTABLE_DELAY);
    }

    private void prepareTooMuchDelay() {
        given(mockAppointment.getTime()).willReturn(APPOINTMENT_TIME_TOO_MUCH_DELAY);
    }

    private void prepareSuccessCreateClientDelay() {
        given(mockCDService.hasSuccess(any())).willReturn(true);
    }

    private void prepareSuccessPlaceInWaitingRoom() {
        given(mockPCWRService.hasSuccess(any())).willReturn(true);
    }

    private void prepareFailPlaceInWaitingRoom() {
        given(mockPCWRService.hasSuccess(any())).willReturn(false);
    }

}
