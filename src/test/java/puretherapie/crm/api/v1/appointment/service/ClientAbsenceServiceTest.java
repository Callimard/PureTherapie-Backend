package puretherapie.crm.api.v1.appointment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientAbsenceRepository;
import puretherapie.crm.data.person.client.Client;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.getMaximumClientDelay;

@SpringBootTest
@DisplayName("ClientAbsenceService tests")
public class ClientAbsenceServiceTest {

    @Autowired
    private ClientAbsenceService cas;

    @Nested
    @DisplayName("Check client absence tests")
    class CheckClientAbsence {

        @Test
        @DisplayName("Test if all client absence are checked")
        void testCheckClientAbsence() {
            prepareAppointmentRepository();
            prepareDelayOnAppointment();

            cas.checkClientAbsence();

            verify(mockCARepository, times(1)).save(any());
            verify(mockAppointmentRepository, times(1)).save(mockAppointment);
            verify(mockAppointment, times(1)).setCanceled(true);
        }

    }

    // Context

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;

    @MockBean
    private ClientAbsenceRepository mockCARepository;

    private void prepareAppointmentRepository() {
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(mockAppointment);
        given(mockAppointmentRepository.findByDay(any())).willReturn(appointmentList);
    }

    private void prepareDelayOnAppointment() {
        given(mockAppointment.isCanceled()).willReturn(false);
        given(mockAppointment.getClientArrival()).willReturn(null);
        given(mockAppointment.getClient()).willReturn(Client.builder().build());
        given(mockAppointment.getTime()).willReturn(LocalTime.now().minusMinutes(getMaximumClientDelay() + 3));
    }
}
