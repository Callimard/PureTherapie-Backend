package puretherapie.crm.api.v1.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"insert_client_technician_aesthetic_care.sql", "insert_time_slot.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"delete_appointment.sql", "delete_time_slot.sql",
                                                                       "delete_client_technician_aesthetic_care.sql"})
@DisplayName("AppointmentCreationService tests")
public class AppointmentCreationServiceTest {

    private static final String CLIENT_EMAIL = "client@email.fr";
    private static final String TECHNICIAN_EMAIL = "technician@email.fr";
    private static final String AESTHETIC_CARE_NAME = "AC";

    private static final LocalDate DAY = LocalDate.of(2022, 1, 1);
    private static final LocalTime NOT_FREE_TIME = LocalTime.of(10, 15);
    private static final LocalTime BEFORE_OVERLAP = LocalTime.of(12, 15);
    private static final LocalTime AFTER_OVERLAP = LocalTime.of(11, 45);
    private static final LocalTime FREE_TIME_SLOT = LocalTime.of(14, 20);

    @Autowired
    private AppointmentCreationService appointmentCreationService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private AestheticCareRepository aestheticCareRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @MockBean
    private AppointmentRepository mockAppointmentRepository;

    private Client c;
    private Technician t;
    private AestheticCare ac;

    @BeforeEach
    void setUp() {
        c = clientRepository.findByEmail(CLIENT_EMAIL);
        t = technicianRepository.findByEmail(TECHNICIAN_EMAIL);
        ac = aestheticCareRepository.findByName(AESTHETIC_CARE_NAME);
    }

    @Nested
    @DisplayName("CreateAppointment tests")
    class CreateAppointment {

        @Test
        @DisplayName("Test with not found client, technician or aesthetic care returns false")
        void testWithNotCorrectId() {
            boolean success = appointmentCreationService.createAppointment(-1, t.getIdPerson(), ac.getIdAestheticCare(), DAY, NOT_FREE_TIME);
            assertThat(success).isFalse();

            success = appointmentCreationService.createAppointment(c.getIdPerson(), -1, ac.getIdAestheticCare(), DAY, NOT_FREE_TIME);
            assertThat(success).isFalse();

            success = appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), -1, DAY, NOT_FREE_TIME);
            assertThat(success).isFalse();

            success = appointmentCreationService.createAppointment(-1, -1, -1, DAY, NOT_FREE_TIME);
            assertThat(success).isFalse();
        }

        @Test
        @DisplayName("Test with not free time slot returns false")
        void testWithNotFreeTimeSlot() {
            boolean success =
                    appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), ac.getIdAestheticCare(), DAY, NOT_FREE_TIME);
            assertThat(success).isFalse();
        }

        @Test
        @DisplayName("Test with before overlap returns false")
        void testWithBeforeOverlap() {
            boolean success =
                    appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), ac.getIdAestheticCare(), DAY, BEFORE_OVERLAP);
            assertThat(success).isFalse();
        }

        @Test
        @DisplayName("Test with after overlap returns false")
        void testWithAfterOverlap() {
            boolean success =
                    appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), ac.getIdAestheticCare(), DAY, AFTER_OVERLAP);
            assertThat(success).isFalse();
        }

        @Test
        @DisplayName("Test with free time slot returns true")
        void testWithAllOk() {
            boolean success =
                    appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), ac.getIdAestheticCare(), DAY, FREE_TIME_SLOT);
            assertThat(success).isTrue();

            TimeSlot timeSlot = timeSlotRepository.findByTechnicianAndDayAndBegin(t, DAY, FREE_TIME_SLOT);
            assertThat(timeSlot).isNotNull();
            verify(mockAppointmentRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Test with fail to save appointment rollback and returns false")
        void testWithFailAppointmentSave() {
            given(mockAppointmentRepository.save(any())).willThrow(new IllegalArgumentException());

            boolean success =
                    appointmentCreationService.createAppointment(c.getIdPerson(), t.getIdPerson(), ac.getIdAestheticCare(), DAY, FREE_TIME_SLOT);
            assertThat(success).isFalse();

            TimeSlot timeSlot = timeSlotRepository.findByTechnicianAndDayAndBegin(t, DAY, FREE_TIME_SLOT);
            assertThat(timeSlot).isNull();
        }

    }

}
