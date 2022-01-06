package puretherapie.crm.api.v1.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.agenda.*;
import puretherapie.crm.data.agenda.repository.*;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.LaunchBreakRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"insert_client_technician_aesthetic_care.sql",
                                                                        "insert_time_slot_appointment.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"delete_appointment.sql", "delete_time_slot.sql",
                                                                       "delete_client_technician_aesthetic_care.sql"})
@DisplayName("AppointmentCreationService tests")
public class AppointmentCreationServiceTest {

    private static final String CLIENT_EMAIL = "client@email.fr";
    private static final String TECHNICIAN_EMAIL = "tech@email.fr";
    private static final String AESTHETIC_CARE_NAME = "AC";

    private static final LocalDate DAY = LocalDate.of(2022, 1, 1);
    private static final LocalTime NOT_FREE_TIME = LocalTime.of(10, 15);
    private static final LocalTime BEFORE_OVERLAP = LocalTime.of(12, 15);
    private static final LocalTime AFTER_OVERLAP = LocalTime.of(11, 45);
    private static final LocalTime IN_FOLLOWING_APPOINTMENT = LocalTime.of(13, 35);
    private static final LocalTime FREE_TIME_SLOT = LocalTime.of(14, 20);
    private static final LocalTime FIVE_MIN_BEFORE_OVER_LAP = LocalTime.of(14, 15);
    private static final LocalTime FIVE_MIN_AFTER_OVER_LAP = LocalTime.of(11, 25);
    private static final LocalTime TEN_MIN_BEFORE_OVER_LAP = LocalTime.of(14, 10);
    private static final LocalTime TEN_MIN_AFTER_OVER_LAP = LocalTime.of(11, 30);
    private static final LocalTime NO_OVERLAP_POSSIBLE = LocalTime.of(14, 0);

    @Autowired
    private AppointmentCreationService appointmentCreationService;


    @Nested
    @DisplayName("CreateAppointment tests")
    class CreateAppointment {

    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 11;
    private static final String CLIENT_FIRST_NAME = "client";
    private static final String CLIENT_LAST_NAME = "client";

    @MockBean
    private TechnicianRepository mockTechnicianRepository;
    @Mock
    private Technician mockTechnician;
    private static final int TECHNICIAN_ID = 12;
    private static final String TECHNICIAN_FIRST_NAME = "technician";
    private static final String TECHNICIAN_LAST_NAME = "technician";

    @MockBean
    private AestheticCareRepository mockACRepository;
    @Mock
    private AestheticCare mockAestheticCare;
    private static final int AESTHETIC_CARE_ID = 2;
    private static final int AESTHETIC_CARE_TIME_EXECUTION = 40;

    @MockBean
    private TimeSlotRepository mockTimeSlotRepository;
    @Mock
    private TimeSlot mockTS1_1;
    @Mock
    private TimeSlot mockTS1_2;
    @Mock
    private TimeSlot mockTS2_1;

    @MockBean
    private AppointmentRepository mockAppointmentRepository;

    @MockBean
    private TimeSlotAtomRepository mockTSARepository;
    @Mock
    private TimeSlotAtom mockTSA;
    @Mock
    private TimeSlotAtom mockNonRelevantTSA;
    private static final int TSA_NB_MINUTE = 30;
    private static final LocalDate TSA_EFFECTIVE_DATE = LocalDate.of(2022, 1, 1);
    private static final LocalDate TSA_NON_RELEVANT_EFFECTIVE_DATE = LocalDate.of(2021, 9, 3);

    @MockBean
    private GlobalOpeningTimeRepository mockGOTRepository;
    @Mock
    private GlobalOpeningTime mockMondayOpening;
    @Mock
    private GlobalOpeningTime mockTuesdayOpening;
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);

    @MockBean
    private ExceptionalCloseRepository mockECRepository;
    @Mock
    private ExceptionalClose mockEC;
    private static final LocalDate EC_DATE = LocalDate.of(2022, 1, 4); // Tuesday

    @MockBean
    private ExceptionalOpeningRepository mockEORepository;
    @Mock
    private ExceptionalOpening mockEO;
    private static final LocalDate EO_DATE = LocalDate.of(2022, 1, 2); // Sunday

    @MockBean
    private LaunchBreakRepository mockLBRepository;
    @Mock
    private LaunchBreak mockLB;
    private static final LocalDate LAUNCH_BREAK_DAY = LocalDate.of(2022, 1, 3); // Monday

    @MockBean
    private NotificationCreationService mockNotificationCreationService;

    private void prepareMinimalContext() {
        prepareClientRepository();
        prepareTechnicianRepository();
        prepareACRepository();
        prepareTSARepository();
    }

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareTechnicianRepository() {
        given(mockTechnicianRepository.findByIdPerson(TECHNICIAN_ID)).willReturn(mockTechnician);
    }

    private void prepareACRepository() {
        given(mockACRepository.findByIdAestheticCare(AESTHETIC_CARE_ID)).willReturn(mockAestheticCare);
    }

    private void prepareTSARepository() {
        List<TimeSlotAtom> listTSA = new ArrayList<>();
        listTSA.add(mockTSA);
        listTSA.add(mockNonRelevantTSA);
        given(mockTSARepository.findAllByOrderByEffectiveDate()).willReturn(listTSA);
    }

    private void prepareTimeSlotRepository() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(mockTS1_1);
        timeSlots.add(mockTS1_2);
        timeSlots.add(mockTS2_1);
        given(mockTimeSlotRepository.findByTechnicianAndDay(mockTechnician, any())).willReturn(timeSlots);
    }

    private void prepareNotificationCreationService() {
        given(mockNotificationCreationService.createNotification(anyString(), anyString(), anyString(), anyBoolean())).willReturn(true);
    }

    private void prepareGORepository() {
        List<GlobalOpeningTime> monday = new ArrayList<>();
        monday.add(mockMondayOpening);
        given(mockGOTRepository.findByDay(DayOfWeek.MONDAY.getValue())).willReturn(monday);

        List<GlobalOpeningTime> tuesday = new ArrayList<>();
        tuesday.add(mockTuesdayOpening);
        given(mockGOTRepository.findByDay(DayOfWeek.TUESDAY.getValue())).willReturn(tuesday);
    }

    private void prepareECRepository() {
        given(mockECRepository.findByDay(EC_DATE)).willReturn(mockEC);
    }

    private void prepareEORepository() {
        List<ExceptionalOpening> eo = new ArrayList<>();
        eo.add(mockEO);
        given(mockEORepository.findByDay(EO_DATE)).willReturn(eo);
    }

    private void prepareLBRepository() {
        given(mockLBRepository.findByTechnicianAndDay(mockTechnician, LAUNCH_BREAK_DAY)).willReturn(mockLB);
    }

    private void prepareClient() {
        given(mockClient.getIdPerson()).willReturn(CLIENT_ID);
        given(mockClient.getFirstName()).willReturn(CLIENT_FIRST_NAME);
        given(mockClient.getLastName()).willReturn(CLIENT_LAST_NAME);
    }

    private void prepareTechnician() {
        given(mockTechnician.getIdPerson()).willReturn(TECHNICIAN_ID);
        given(mockTechnician.getFirstName()).willReturn(TECHNICIAN_FIRST_NAME);
        given(mockTechnician.getLastName()).willReturn(TECHNICIAN_LAST_NAME);
    }

    private void prepareAestheticCare() {
        given(mockAestheticCare.getIdAestheticCare()).willReturn(AESTHETIC_CARE_ID);
        given(mockAestheticCare.getTimeExecution()).willReturn(AESTHETIC_CARE_TIME_EXECUTION);
    }

    private void prepareTSA() {
        given(mockTSA.getNumberOfMinutes()).willReturn(TSA_NB_MINUTE);
        given(mockTSA.getEffectiveDate()).willReturn(TSA_EFFECTIVE_DATE);
    }

    private void prepareNonRelevantTSA() {
        given(mockNonRelevantTSA.getEffectiveDate()).willReturn(TSA_NON_RELEVANT_EFFECTIVE_DATE);
    }

    private void prepareMondayOpening() {
        given(mockMondayOpening.getOpeningTime()).willReturn(OPENING_TIME);
        given(mockMondayOpening.openingTime()).willReturn(OPENING_TIME);
        given(mockMondayOpening.getCloseTime()).willReturn(CLOSE_TIME);
        given(mockMondayOpening.closeTime()).willReturn(CLOSE_TIME);
    }

    private void prepareTuesdayOpening() {
        given(mockTuesdayOpening.getOpeningTime()).willReturn(OPENING_TIME);
        given(mockTuesdayOpening.openingTime()).willReturn(OPENING_TIME);
        given(mockTuesdayOpening.getCloseTime()).willReturn(CLOSE_TIME);
        given(mockTuesdayOpening.closeTime()).willReturn(CLOSE_TIME);
    }

}
