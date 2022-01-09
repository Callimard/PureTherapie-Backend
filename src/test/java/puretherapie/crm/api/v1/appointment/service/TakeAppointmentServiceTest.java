package puretherapie.crm.api.v1.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.agenda.*;
import puretherapie.crm.data.agenda.repository.*;
import puretherapie.crm.data.appointment.Appointment;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.appointment.service.TakeAppointmentService.*;

@Slf4j
@SpringBootTest
@DisplayName("AppointmentCreationService tests")
public class TakeAppointmentServiceTest {

    private static final LocalDate CORRECT_RANDOM_DATE = LocalDate.of(1996, 9, 3);
    private static final LocalTime CORRECT_RANDOM_TIME = LocalTime.of(10, 50);

    @Autowired
    private TakeAppointmentService tas;

    @Nested
    @DisplayName("CreateAppointment tests")
    class CreateAppointment {

        @Test
        @DisplayName("Test with null day or null beginTime fail")
        void testWithNullDayOrNullBeginTime() {
            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, null, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, NULL_DAY_OR_BEGIN_TIME_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, CORRECT_RANDOM_DATE, null);
            verifyFail(res);
            verifyFailType(res, NULL_DAY_OR_BEGIN_TIME_ERROR);
        }

        @Test
        @DisplayName("Test with non correct client id fail")
        void testWithNonCorrectClientId() {
            prepareTechnicianRepository();
            prepareACRepository();
            Map<String, Object> res = tas.takeAppointment(1, TECHNICIAN_ID, AC_ID, CORRECT_RANDOM_DATE, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, CLIENT_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with non correct technician id fail")
        void testWithNonCorrectTechnicianId() {
            prepareClientRepository();
            prepareACRepository();
            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, 1, AC_ID, CORRECT_RANDOM_DATE, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, TECHNICIAN_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with non correct aesthetic care id fail")
        void testWithNonCorrectAestheticCareId() {
            prepareClientRepository();
            prepareTechnicianRepository();
            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, 1, CORRECT_RANDOM_DATE, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, AESTHETIC_CARE_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with exceptional close fail")
        void testWithExceptionalClose() {
            prepareMinimalContext();
            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, EC_DATE, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, EXCEPTIONAL_CLOSE_ERROR);
        }

        @Test
        @DisplayName("Test with not open day fail")
        void testWithNotOpenDay() {
            prepareMinimalContext();
            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, NOT_OPEN_DAY, CORRECT_RANDOM_TIME);
            verifyFail(res);
            verifyFailType(res, NOT_OPEN_ERROR);
        }

        @Test
        @DisplayName("Test with not in opening time appointment fail")
        void testWithNotInOpeningTime() {
            prepareMinimalContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, BEFORE_OPENING_TIME);
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, AFTER_CLOSE_TIME);
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, TUESDAY_DATE, BEFORE_OPENING_TIME);
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, TUESDAY_DATE, AFTER_CLOSE_TIME);
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, TUESDAY_DATE, CLOSE_TIME.minusMinutes(15));
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);
        }

        @Test
        @DisplayName("Test with in launch break fail")
        void testInLaunchBreak() {
            prepareMinimalContext();
            prepareLBRepository();
            prepareLB();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, LAUNCH_BREAK_BEGIN_TIME);
            verifyFail(res);
            verifyFailType(res, DURING_LAUNCH_BREAK_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, LAUNCH_BREAK_BEGIN_TIME.minusMinutes(15));
            verifyFail(res);
            verifyFailType(res, DURING_LAUNCH_BREAK_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, LAUNCH_BREAK_BEGIN_TIME.plusMinutes(15));
            verifyFail(res);
            verifyFailType(res, DURING_LAUNCH_BREAK_ERROR);
        }

        @Test
        @DisplayName("Test with incompatible time slot time fail")
        void testWithIncompatibleTimeSlotTime() {
            prepareMinimalContext();
            prepareLBRepository();
            prepareLB();


            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, INCOMPATIBLE_TIME_SLOT_TIME_1);
            verifyFail(res);
            verifyFailType(res, INCOMPATIBLE_TIME_SLOT_TIME);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, INCOMPATIBLE_TIME_SLOT_TIME_2);
            verifyFail(res);
            verifyFailType(res, INCOMPATIBLE_TIME_SLOT_TIME);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, INCOMPATIBLE_TIME_SLOT_TIME_3);
            verifyFail(res);
            verifyFailType(res, INCOMPATIBLE_TIME_SLOT_TIME);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, INCOMPATIBLE_TIME_SLOT_TIME_4);
            verifyFail(res);
            verifyFailType(res, INCOMPATIBLE_TIME_SLOT_TIME);
        }

        @Test
        @DisplayName("Test with overlap time slot fail")
        void testWithOverlap() {
            prepareMinimalContext();
            prepareLBRepository();
            prepareLB();
            prepareTimeSlotRepository();
            prepareTimeSlot();
            prepareAC();
            prepareMedAC();
            prepareLongAC();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, AC_OVERLAP_TS1_1);
            verifyFail(res);
            verifyFailType(res, OVERLAP_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, MED_AC_ID, MONDAY_DATE, MED_AC_OVERLAP_TS1_2);
            verifyFail(res);
            verifyFailType(res, OVERLAP_ERROR);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, LONG_AC_ID, MONDAY_DATE, LONG_AC_OVERLAP_TS2_2);
            verifyFail(res);
            verifyFailType(res, OVERLAP_ERROR);
        }

        @Test
        @DisplayName("Test with just begin at the end of an other appointment success")
        void testWithBeginAtTheEndOfOtherAppointment() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE,
                                                          TS1_2_BEGIN_TIME.plusMinutes(TSA_NB_MINUTE), false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, TUESDAY_DATE,
                                      TS1_2_BEGIN_TIME.plusMinutes(TSA_NB_MINUTE), false);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with just end at the begin of other appointment success")
        void testWithFinishJustAtTheBeginOtherAppointment() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE,
                                                          TS2_1_BEGIN_TIME.minusMinutes(AC_TIME_EXECUTION), false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, LONG_AC_ID, MONDAY_DATE,
                                      TS2_1_BEGIN_TIME.minusMinutes(LONG_AC_TIME_EXECUTION), false);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with just begin at the end of launch break success")
        void testWithJustBeginAtTheEndOfLB() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE,
                                                          LAUNCH_BREAK_BEGIN_TIME.plusMinutes(LAUNCH_BREAK_DURATION), false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, TUESDAY_DATE,
                                      LAUNCH_BREAK_BEGIN_TIME.plusMinutes(LAUNCH_BREAK_DURATION), false);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with just end at the begin of a launch break success")
        void testWithJustEndAtTheBeginOfLB() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE,
                                                          LAUNCH_BREAK_BEGIN_TIME.minusMinutes(AC_TIME_EXECUTION), false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, LONG_AC_ID, MONDAY_DATE,
                                      LAUNCH_BREAK_BEGIN_TIME.minusMinutes(LONG_AC_TIME_EXECUTION), false);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with overlap with free time slot success")
        void testWithOverlapWithFreeTimeSlot() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, MONDAY_DATE, TS_FREE_BEGIN_TIME, false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, LONG_AC_ID, MONDAY_DATE,
                                      TS_FREE_BEGIN_TIME.minusMinutes(LONG_AC_TIME_EXECUTION - TSA_NB_MINUTE), false);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with exceptional opening success")
        void testWithExceptionalOpeningSuccess() {
            prepareSuccessContext();

            Map<String, Object> res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, EO_DATE, EO_OPENING_TIME, false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, EO_DATE, EO_CLOSE_TIME.minusMinutes(AC_TIME_EXECUTION), false);
            verifySuccess(res);

            res = tas.takeAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID, EO_DATE, EO_CLOSE_TIME.plusMinutes(TSA_NB_MINUTE), false);
            verifyFail(res);
            verifyFailType(res, NOT_IN_OPENING_TIME_ERROR);
        }

    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(tas.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(tas.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(tas.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 11;

    @MockBean
    private TechnicianRepository mockTechnicianRepository;
    @Mock
    private Technician mockTechnician;
    private static final int TECHNICIAN_ID = 12;

    @MockBean
    private AestheticCareRepository mockACRepository;
    @Mock
    private AestheticCare mockAC;
    private static final int AC_ID = 2;
    private static final int AC_TIME_EXECUTION = 30;
    @Mock
    private AestheticCare mockMediumAC;
    private static final int MED_AC_ID = 1;
    private static final int MED_AC_TIME_EXECUTION = 45;
    @Mock
    private AestheticCare mockLongAC;
    private static final int LONG_AC_ID = 3;
    private static final int LONG_AC_TIME_EXECUTION = 90;

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
    private TimeSlotRepository mockTimeSlotRepository;
    @Mock
    private TimeSlot mockTS1_1;
    private static final LocalTime TS1_1_BEGIN_TIME = LocalTime.of(8, 0);
    @Mock
    private TimeSlot mockTS1_2;
    private static final LocalTime TS1_2_BEGIN_TIME = LocalTime.of(8, TSA_NB_MINUTE);
    @Mock
    private TimeSlot mockTS2_1;
    @Mock
    private TimeSlot mockTSFree;
    private static final LocalTime TS_FREE_BEGIN_TIME = LocalTime.of(17, 0);
    private static final LocalTime TS2_1_BEGIN_TIME = LocalTime.of(12, 0);
    private static final LocalTime INCOMPATIBLE_TIME_SLOT_TIME_1 = LocalTime.of(10, 5);
    private static final LocalTime INCOMPATIBLE_TIME_SLOT_TIME_2 = LocalTime.of(10, 15);
    private static final LocalTime INCOMPATIBLE_TIME_SLOT_TIME_3 = LocalTime.of(10, 3);
    private static final LocalTime INCOMPATIBLE_TIME_SLOT_TIME_4 = LocalTime.of(10, 29);
    private static final LocalTime AC_OVERLAP_TS1_1 = TS1_1_BEGIN_TIME;
    private static final LocalTime MED_AC_OVERLAP_TS1_2 = TS1_1_BEGIN_TIME.minusMinutes(TSA_NB_MINUTE);
    private static final LocalTime LONG_AC_OVERLAP_TS2_2 = TS2_1_BEGIN_TIME.minusMinutes(TSA_NB_MINUTE);

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;

    @MockBean
    private GlobalOpeningTimeRepository mockGOTRepository;
    @Mock
    private GlobalOpeningTime mockMondayOpening;
    @Mock
    private GlobalOpeningTime mockTuesdayOpening;
    private static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);
    private static final LocalTime BEFORE_OPENING_TIME = OPENING_TIME.minusMinutes(15);
    private static final LocalTime AFTER_CLOSE_TIME = CLOSE_TIME.plusMinutes(15);
    private static final LocalDate MONDAY_DATE = LocalDate.of(2022, 1, 10); // Monday
    private static final LocalDate TUESDAY_DATE = LocalDate.of(2022, 1, 11); // Tuesday
    private static final LocalDate NOT_OPEN_DAY = LocalDate.of(2022, 1, 5); // Wednesday

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
    private static final LocalTime EO_OPENING_TIME = LocalTime.of(16, 0);
    private static final LocalTime EO_CLOSE_TIME = LocalTime.of(18, 0);

    @MockBean
    private LaunchBreakRepository mockLBRepository;
    @Mock
    private LaunchBreak mockLB;
    private static final LocalTime LAUNCH_BREAK_BEGIN_TIME = LocalTime.of(14, 0);
    private static final int LAUNCH_BREAK_DURATION = 60;

    @MockBean
    private NotificationCreationService mockNotificationCreationService;

    private void prepareMinimalContext() {
        prepareClientRepository();
        prepareTechnicianRepository();
        prepareACRepository();
        prepareTSARepository();
        prepareTSA();
        prepareNonRelevantTSA();
        prepareGORepository();
        prepareMondayOpening();
        prepareTuesdayOpening();
        prepareEORepository();
        prepareEO();
        prepareECRepository();
    }

    private void prepareSuccessContext() {
        prepareMinimalContext();
        prepareLBRepository();
        prepareLB();
        prepareAppointmentRepository();
        prepareTimeSlotRepository();
        prepareTimeSlot();
        prepareAC();
        prepareMedAC();
        prepareLongAC();
        prepareNotificationCreationService();
    }

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareTechnicianRepository() {
        given(mockTechnicianRepository.findByIdPerson(TECHNICIAN_ID)).willReturn(mockTechnician);
    }

    private void prepareACRepository() {
        given(mockACRepository.findByIdAestheticCare(AC_ID)).willReturn(mockAC);
        given(mockACRepository.findByIdAestheticCare(MED_AC_ID)).willReturn(mockMediumAC);
        given(mockACRepository.findByIdAestheticCare(LONG_AC_ID)).willReturn(mockLongAC);
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
        timeSlots.add(mockTSFree);
        given(mockTimeSlotRepository.findByTechnicianAndDay(eq(mockTechnician), any())).willReturn(timeSlots);
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
        given(mockLBRepository.findByTechnicianAndDay(eq(mockTechnician), any())).willReturn(mockLB);
    }

    private void prepareAppointmentRepository() {
        given(mockAppointmentRepository.save(any())).willReturn(mockAppointment);
    }

    private void prepareAC() {
        given(mockAC.getIdAestheticCare()).willReturn(AC_ID);
        given(mockAC.getTimeExecution()).willReturn(AC_TIME_EXECUTION);
    }

    private void prepareMedAC() {
        given(mockMediumAC.getIdAestheticCare()).willReturn(MED_AC_ID);
        given(mockMediumAC.getTimeExecution()).willReturn(MED_AC_TIME_EXECUTION);
    }

    private void prepareLongAC() {
        given(mockLongAC.getIdAestheticCare()).willReturn(LONG_AC_ID);
        given(mockLongAC.getTimeExecution()).willReturn(LONG_AC_TIME_EXECUTION);
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

    private void prepareEO() {
        given(mockEO.getOpeningTime()).willReturn(EO_OPENING_TIME);
        given(mockEO.openingTime()).willReturn(EO_OPENING_TIME);
        given(mockEO.getCloseTime()).willReturn(EO_CLOSE_TIME);
        given(mockEO.closeTime()).willReturn(EO_CLOSE_TIME);
    }

    private void prepareTimeSlot() {
        given(mockTS1_1.getBegin()).willReturn(TS1_1_BEGIN_TIME);
        given(mockTS1_1.getTime()).willReturn(TSA_NB_MINUTE);
        given(mockTSFree.isFree()).willReturn(false);

        given(mockTS1_2.getBegin()).willReturn(TS1_2_BEGIN_TIME);
        given(mockTS1_2.getTime()).willReturn(TSA_NB_MINUTE);
        given(mockTSFree.isFree()).willReturn(false);

        given(mockTS2_1.getBegin()).willReturn(TS2_1_BEGIN_TIME);
        given(mockTS2_1.getTime()).willReturn(TSA_NB_MINUTE);
        given(mockTSFree.isFree()).willReturn(false);

        given(mockTSFree.getBegin()).willReturn(TS_FREE_BEGIN_TIME);
        given(mockTSFree.getTime()).willReturn(TSA_NB_MINUTE);
        given(mockTSFree.isFree()).willReturn(true);
    }

    private void prepareLB() {
        given(mockLB.getBeginHour()).willReturn(LAUNCH_BREAK_BEGIN_TIME);
        given(mockLB.getDuration()).willReturn(LAUNCH_BREAK_DURATION);
    }

}
