package puretherapie.crm.api.v1.waitingroom.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.waitingroom.service.PlaceInWaitingRoomService.*;

@SpringBootTest
@DisplayName("PlaceClientInWaitingRoomService tests")
public class PlaceInWaitingRoomServiceTest {

    @Autowired
    private PlaceInWaitingRoomService pcs;

    // Tests.

    @Nested
    @DisplayName("Test place client in waiting room")
    class PlaceClientInWaitingRoom {

        @Test
        @DisplayName("Test with client not found fail")
        void testWithNotFoundClient() {
            prepareClientRepository();
            Map<String, Object> res = pcs.placeInWaitingRoom(-1, APPOINTMENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with client appointment not associate to client fail")
        void testWithNotFoundIdAppointment() {
            prepareClientRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareNotAssociatedAppointment();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID, APPOINTMENT_ID);
            verifyFail(res);
            verifyFailType(res, NON_COHERENCE_BETWEEN_CLIENT_APPOINTMENT_ERROR);
        }

        @Test
        @DisplayName("Test with client appointment canceled fail")
        void testWithCanceledClientAppointment() {
            prepareClientRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareCoherentAppointment();
            prepareCanceledAppointment();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID, APPOINTMENT_ID);
            verifyFail(res);
            verifyFailType(res, APPOINTMENT_CANCELED_ERROR);
        }

        @Test
        @DisplayName("Test with free time slot fail")
        void testWithFreeTimeSlot() {
            prepareClientRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareCoherentAppointment();
            prepareTimeSlotRepository();
            prepareFreeTimeSlot();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID, APPOINTMENT_ID);
            verifyFail(res);
            verifyFailType(res, TIME_SLOT_INCOHERENCE_ERROR);
        }

        @Test
        @DisplayName("Test with not for today appointment fail")
        void testWithNotForTodayAppointment() {
            prepareClientRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareCoherentAppointment();
            prepareNotForTodayAppointment();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID, APPOINTMENT_ID);
            verifyFail(res);
            verifyFailType(res, APPOINTMENT_NOT_FOR_TODAY_ERROR);
        }

        @Test
        @DisplayName("Test with correct client and appointment success")
        void testWithCorrectClientAndAppointment() {
            prepareClientRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareCoherentAppointment();
            prepareTodayAppointment();
            prepareWRRepository();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID, APPOINTMENT_ID);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with unknown appointment success")
        void testWithUnknownAppointment() {
            prepareClientRepository();
            prepareClient();

            Map<String, Object> res = pcs.placeInWaitingRoom(CLIENT_ID);
            verifySuccess(res);
        }
    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(pcs.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(pcs.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(pcs.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    @Mock
    private Client mockOtherClient;
    private static final int CLIENT_ID = 3;

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;
    private static final int APPOINTMENT_ID = 15;

    @MockBean
    private TimeSlotRepository mockTSRepository;
    @Mock
    private TimeSlot mockTS;
    @Mock
    private TimeSlot mockOtherTS;

    @MockBean
    private WaitingRoomRepository mockWRRepository;
    @Mock
    private WaitingRoom mockWR;

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareClient() {
        given(mockClient.getIdPerson()).willReturn(CLIENT_ID);
    }

    private void prepareAppointmentRepository() {
        given(mockAppointmentRepository.findByIdAppointment(APPOINTMENT_ID)).willReturn(mockAppointment);
    }

    private void prepareCoherentAppointment() {
        given((mockAppointment.getClient())).willReturn(mockClient);
    }

    private void prepareNotAssociatedAppointment() {
        given(mockAppointment.getClient()).willReturn(mockOtherClient);
        given(mockOtherClient.getIdPerson()).willReturn(CLIENT_ID - 1);
    }

    private void prepareCanceledAppointment() {
        given(mockAppointment.isCanceled()).willReturn(true);
    }

    private void prepareTodayAppointment() {
        prepareTimeSlotRepository();
        given(mockTS.getDay()).willReturn(LocalDate.now());
    }

    private void prepareNotForTodayAppointment() {
        prepareTimeSlotRepository();
        given(mockTS.getDay()).willReturn(LocalDate.now().minusDays(1L));
    }

    private void prepareTimeSlotRepository() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(mockTS);
        timeSlots.add(mockOtherTS);
        given(mockTSRepository.findByAppointmentOrderByBeginAsc(mockAppointment)).willReturn(timeSlots);
    }

    private void prepareFreeTimeSlot() {
        prepareTimeSlotRepository();
        given(mockOtherTS.isFree()).willReturn(true);
    }

    private void prepareWRRepository() {
        given(mockWRRepository.save(any())).willReturn(mockWR);
    }

}
