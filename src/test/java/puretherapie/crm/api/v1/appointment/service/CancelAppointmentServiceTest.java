package puretherapie.crm.api.v1.appointment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static puretherapie.crm.api.v1.appointment.service.CancelAppointmentService.APPOINTMENT_NOT_FOUND_ERROR;

@SpringBootTest
@DisplayName("CancelAppointmentService tests")
public class CancelAppointmentServiceTest {

    @Autowired
    private CancelAppointmentService cas;

    @Nested
    @DisplayName("Cancel appointment test")
    class CancelAppointment {

        @Test
        @DisplayName("Test with not found appointment fail")
        void testWithNotFoundAppointment() {
            prepareAppointmentRepository();

            SimpleResponseDTO res = cas.cancelAppointment(133);
            verifyFail(res);
            verifyFailType(res, APPOINTMENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not canceled appointment success")
        void testWithNotCanceledAppointment() {
            prepareAppointmentRepository();
            prepareTimeSlotRepository();
            prepareNotCanceledAppointment();

            SimpleResponseDTO res = cas.cancelAppointment(APPOINTMENT_ID);
            verifySuccess(res);

            verify(mockAppointmentRepository, times(1)).save(mockAppointment);
            verify(mockTSRepository, times(1)).saveAll(any());
            verify(mockAppointment, times(1)).setCanceled(true);
            verify(mockTS1, times(1)).setFree(true);
            verify(mockTS2, times(1)).setFree(true);
        }

        @Test
        @DisplayName("Test with already canceled appointment success")
        void testWithAlreadyCanceledAppointment() {
            prepareAppointmentRepository();
            prepareTimeSlotRepository();
            prepareAlreadyCanceledAppointment();

            SimpleResponseDTO res = cas.cancelAppointment(APPOINTMENT_ID);
            verifySuccess(res);

            verify(mockAppointmentRepository, times(0)).save(mockAppointment);
            verify(mockTSRepository, times(0)).saveAll(any());
            verify(mockAppointment, times(0)).setCanceled(true);
            verify(mockTS1, times(0)).setFree(true);
            verify(mockTS2, times(0)).setFree(true);
        }

    }

    private void verifySuccess(SimpleResponseDTO res) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isTrue();
    }

    private void verifyFail(SimpleResponseDTO res) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
    }

    void verifyFailType(SimpleResponseDTO res, String expectedMsg) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
        assertThat(res.message()).isEqualTo(expectedMsg);
    }

    // Context.

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;
    private static final int APPOINTMENT_ID = 588;

    @MockBean
    private TimeSlotRepository mockTSRepository;
    @Mock
    private TimeSlot mockTS1;
    @Mock
    private TimeSlot mockTS2;

    private void prepareAppointmentRepository() {
        given(mockAppointmentRepository.findByIdAppointment(APPOINTMENT_ID)).willReturn(mockAppointment);
    }

    private void prepareNotCanceledAppointment() {
        given(mockAppointment.isCanceled()).willReturn(false);
    }

    private void prepareAlreadyCanceledAppointment() {
        given(mockAppointment.isCanceled()).willReturn(true);
    }

    private void prepareTimeSlotRepository() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(mockTS1);
        timeSlots.add(mockTS2);
        given(mockTSRepository.findByAppointment(mockAppointment)).willReturn(timeSlots);
    }

}
