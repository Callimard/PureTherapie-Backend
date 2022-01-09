package puretherapie.crm.api.v1.product.aesthetic.care.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.product.aesthetic.care.service.AestheticCareProvisionCreationService.*;

@SpringBootTest
@DisplayName("AestheticCareProvisionCreationService test")
public class AestheticCareProvisionCreationServiceTest {

    @Autowired
    private AestheticCareProvisionCreationService acps;

    // Tests.

    @Nested
    @DisplayName("Aesthetic cre provision creation tests")
    class AestheticCareProvision {

        @Test
        @DisplayName("Test with null date time fail")
        void testWithNullDateTime() {
            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, AC_ID, APPOINTMENT_ID, null);
            verifyFail(res);
            verifyFailType(res, NO_DATE_TIME_ERROR);
        }

        @Test
        @DisplayName("Test with not found client id fail")
        void testWithNotFoundClient() {
            prepareMinimalRepository();

            Map<String, Object> res = acps.createAestheticCareProvision(2, TECHNICIAN_ID, AC_ID, APPOINTMENT_ID, OffsetDateTime.now());
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found technician id fail")
        void testWithNotFoundTechnician() {
            prepareMinimalRepository();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, 5, AC_ID, APPOINTMENT_ID, OffsetDateTime.now());
            verifyFail(res);
            verifyFailType(res, TECHNICIAN_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found aesthetic care id fail")
        void testWithNotFoundAestheticCare() {
            prepareMinimalRepository();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, 9, APPOINTMENT_ID, OffsetDateTime.now());
            verifyFail(res);
            verifyFailType(res, AESTHETIC_CARE_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not associated to client appointment fail")
        void testWithNotAssociatedToClientAppointment() {
            prepareMinimalRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareNotAssociatedToClientAppointment();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, AC_ID, APPOINTMENT_ID, OffsetDateTime.now());
            verifyFail(res);
            verifyFailType(res, CLIENT_AND_APPOINTMENT_NOT_ASSOCIATED_ERROR);
        }

        @Test
        @DisplayName("Test with canceled appointment fail")
        void testWithCanceledAppointment() {
            prepareMinimalRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareCanceledAppointment();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, AC_ID, APPOINTMENT_ID, OffsetDateTime.now());
            verifyFail(res);
            verifyFailType(res, APPOINTMENT_CANCELED_ERROR);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testWithAllCorrect() {
            prepareMinimalRepository();
            prepareClient();
            prepareAppointmentRepository();
            prepareAssociatedToClientAppointment();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, AC_ID, APPOINTMENT_ID, OffsetDateTime.now());
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with no appointment success")
        void testWithNoAppointment() {
            prepareMinimalRepository();
            prepareClient();

            Map<String, Object> res = acps.createAestheticCareProvision(CLIENT_ID, TECHNICIAN_ID, AC_ID, -1, OffsetDateTime.now());
            verifySuccess(res);
        }

    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(acps.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(acps.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(acps.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 25;

    @MockBean
    private TechnicianRepository mockTechnicianRepository;
    @Mock
    private Technician mockTechnician;
    private static final int TECHNICIAN_ID = 78;

    @MockBean
    private AestheticCareRepository mockACRepository;
    @Mock
    private AestheticCare mockAC;
    private static final int AC_ID = 10;

    @MockBean
    private AppointmentRepository mockAppointmentRepository;
    @Mock
    private Appointment mockAppointment;
    private static final int APPOINTMENT_ID = 25;

    @MockBean
    private AestheticCareProvisionRepository mockACPRepository;

    private void prepareMinimalRepository() {
        prepareClientRepository();
        prepareTechnicianRepository();
        prepareAestheticCareRepository();
        prepareAppointment();
        prepareACPRepository();
    }

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareClient() {
        given(mockClient.getIdPerson()).willReturn(CLIENT_ID);
    }

    private void prepareTechnicianRepository() {
        given(mockTechnicianRepository.findByIdPerson(TECHNICIAN_ID)).willReturn(mockTechnician);
    }

    private void prepareAestheticCareRepository() {
        given(mockACRepository.findByIdAestheticCare(AC_ID)).willReturn(mockAC);
    }

    private void prepareAppointmentRepository() {
        given(mockAppointmentRepository.findByIdAppointment(APPOINTMENT_ID)).willReturn(mockAppointment);
    }

    private void prepareAppointment() {
        given(mockAppointment.getIdAppointment()).willReturn(APPOINTMENT_ID);
    }

    private void prepareNotAssociatedToClientAppointment() {
        given(mockAppointment.getClient()).willReturn(Client.builder().idPerson(CLIENT_ID - 1).build());
    }

    private void prepareCanceledAppointment() {
        prepareAssociatedToClientAppointment();
        given(mockAppointment.isCanceled()).willReturn(true);
    }

    private void prepareAssociatedToClientAppointment() {
        given(mockAppointment.getClient()).willReturn(mockClient);
    }

    private void prepareACPRepository() {
        given(mockACPRepository.save(any())).willReturn(null);
    }
}
