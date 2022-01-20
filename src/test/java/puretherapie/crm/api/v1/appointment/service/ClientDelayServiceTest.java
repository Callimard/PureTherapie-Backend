package puretherapie.crm.api.v1.appointment.service;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientDelay;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientDelayRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.*;

@DisplayName("ClientDelayService test")
public class ClientDelayServiceTest {

    @Nested
    @DisplayName("Maximum Client Delay test")
    class MaximumClientDelay {

        @Nested
        @DisplayName("Charge Maximum Client delay tests")
        class ChargeMaximumClientDelay {

            @Test
            @DisplayName("Test with no file found, maximumClientDelay is equal to DEFAULT_MAXIMUM_CLIENT_DELAY")
            void testWithNoFileFound() throws IOException {
                Files.deleteIfExists(Path.of(ClientDelayService.MAXIMUM_CLIENT_DELAY_FILE));
                ClientDelayService.rechargeMaximumClientDelay();
                assertThat(ClientDelayService.getMaximumClientDelay()).isEqualByComparingTo(ClientDelayService.DEFAULT_MAXIMUM_CLIENT_DELAY);
            }

            @Test
            @DisplayName("Test with existing file, maximumClientDelay is equal to the value in the file")
            void testWithExistingFile() throws IOException {
                int value = 25;
                writeMaximumClientDelay(value);
                ClientDelayService.rechargeMaximumClientDelay();
                assertThat(ClientDelayService.getMaximumClientDelay()).isEqualByComparingTo(value);
            }
        }

        @Nested
        @DisplayName("Set maximum client delay test")
        class SetMaximumClientDelay {

            @Test
            @DisplayName("Test if setMaximumClientDelay change the value of maximum delay and also the data file")
            void testSetMaximumDelay() throws IOException {
                int value = 35;
                ClientDelayService.setMaximumClientDelay(value);
                assertThat(ClientDelayService.getMaximumClientDelay()).isEqualTo(value);
                assertThat(readMaximumClientDelay()).isEqualByComparingTo(value);
            }

        }

        private static void writeMaximumClientDelay(int maximumClientDelay) throws IOException {
            SimpleService.createDataDirectory();
            try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(ClientDelayService.MAXIMUM_CLIENT_DELAY_FILE))) {
                writer.writeInt(maximumClientDelay);
                writer.flush();
            }
        }

        private static int readMaximumClientDelay() throws IOException {
            try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(ClientDelayService.MAXIMUM_CLIENT_DELAY_FILE))) {
                return reader.readInt();
            }
        }

    }

    @Nested
    @DisplayName("Late methods tests")
    class LateMethods {


        @Nested
        @DisplayName("IsLateFromNow tests")
        class IsLateFromNow {
            @Test
            @DisplayName("Test returns false if time after now")
            void testWithTimeAfterNow() {
                LocalTime time = LocalTime.now().plusMinutes(2);
                assertThat(ClientDelayService.isLateFromNow(time)).isFalse();
            }

            @Test
            @DisplayName("Test returns true if time is before now")
            void testWithTimeBeforeNow() {
                LocalTime time = LocalTime.now().minusMinutes(2);
                assertThat(ClientDelayService.isLateFromNow(time)).isTrue();
            }
        }

        @Nested
        @DisplayName("IsTooMuchLateFromNow tests")
        class IsTooMuchLateFromNow {

            @Test
            @DisplayName("Test returns false if time is before now")
            void testWithTimeBefore() {
                LocalTime time = LocalTime.now().minusMinutes(2);
                assertThat(ClientDelayService.isTooMuchLateFromNow(time)).isFalse();
            }

            @Test
            @DisplayName("Test returns false if time is after but not too much")
            void testWithNotTooMuch() {
                LocalTime time = LocalTime.now().plusMinutes(ClientDelayService.getMaximumClientDelay() - 5);
                assertThat(ClientDelayService.isTooMuchLateFromNow(time)).isFalse();
            }

            @Test
            @DisplayName("Test returns true if time is too much")
            void testWithTooMuch() {
                LocalTime time = LocalTime.now().minusMinutes(ClientDelayService.getMaximumClientDelay() + 5);
                assertThat(ClientDelayService.isTooMuchLateFromNow(time)).isTrue();
            }
        }

        @Nested
        @DisplayName("DelayFromNow tests")
        class DelayFromNow {

            @Test
            @DisplayName("Test returns 0 if appointment time is after now (not late)")
            void testWithAfterNow() {
                LocalTime time = LocalTime.now().plusMinutes(2);
                assertThat(ClientDelayService.delayFromNow(time)).isEqualByComparingTo(0L);
            }

            @Test
            @DisplayName("Test returns correct delay with late time")
            void testWithLateTime() {
                long delay = 3;
                LocalTime time = LocalTime.now().minusMinutes(delay);
                assertThat(ClientDelayService.delayFromNow(time)).isCloseTo(delay, Offset.offset(1L));
            }

        }

    }

    @Nested
    @SpringBootTest
    @DisplayName("CreateClientDelay tests")
    class CreateClientDelay {

        @Autowired
        private ClientDelayService cds;

        @Test
        @DisplayName("Test with unknown client fail")
        void testWithUnknownClient() {
            prepareMinimalRepository();

            Map<String, Object> res = cds.createClientDelay(-1, APPOINTMENT_ID, CORRECT_DELAY);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with unknown client fail")
        void testWithUnknownAppointment() {
            prepareMinimalRepository();

            Map<String, Object> res = cds.createClientDelay(CLIENT_ID, -1, CORRECT_DELAY);
            verifyFail(res);
            verifyFailType(res, APPOINTMENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not client associate to appointment fail")
        void testWithNotClientAssociateToAppointment() {
            prepareMinimalRepository();
            prepareNotAssociateClient();

            Map<String, Object> res = cds.createClientDelay(CLIENT_ID, APPOINTMENT_ID, CORRECT_DELAY);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_ASSOCIATE_TO_APPOINTMENT_ERROR);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testWithAllCorrect() {
            prepareSuccess();

            Map<String, Object> res = cds.createClientDelay(CLIENT_ID, APPOINTMENT_ID, CORRECT_DELAY);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with negative delay success")
        void testWithNegativeDelay() {
            prepareSuccess();

            Map<String, Object> res = cds.createClientDelay(CLIENT_ID, APPOINTMENT_ID, -1);
            verifySuccess(res);
        }

        private void verifySuccess(Map<String, Object> res) {
            assertThat(res).isNotNull().containsKey(cds.getSuccessTag());
        }

        private void verifyFail(Map<String, Object> res) {
            assertThat(res).isNotNull().containsKey(cds.getFailTag());
        }

        void verifyFailType(Map<String, Object> res, String expectedKey) {
            @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(cds.getFailTag());
            assertThat(errors).isNotNull().containsKey(expectedKey);
        }

        // Context.

        @MockBean
        private ClientRepository mockClientRepository;
        @Mock
        private Client mockClient;
        private static final int CLIENT_ID = 96;

        @MockBean
        private AppointmentRepository mockAppointmentRepository;
        @Mock
        private Appointment mockAppointment;
        private static final int APPOINTMENT_ID = 20;

        @MockBean
        private ClientDelayRepository mockCDRepository;
        @Mock
        private ClientDelay mockCD;

        private static final int CORRECT_DELAY = 10;

        private void prepareMinimalRepository() {
            prepareClientRepository();
            prepareAppointmentRepository();
        }

        private void prepareSuccess() {
            prepareMinimalRepository();
            prepareClientDelayRepository();
            prepareAssociateClient();
        }

        private void prepareClientRepository() {
            given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
        }

        private void prepareNotAssociateClient() {
            given(mockClient.isAssociateTo(mockAppointment)).willReturn(false);
        }

        private void prepareAssociateClient() {
            given(mockClient.isAssociateTo(mockAppointment)).willReturn(true);
        }

        private void prepareAppointmentRepository() {
            given(mockAppointmentRepository.findByIdAppointment(APPOINTMENT_ID)).willReturn(mockAppointment);
        }

        private void prepareClientDelayRepository() {
            given(mockCDRepository.save(any())).willReturn(mockCD);
        }
    }

}
