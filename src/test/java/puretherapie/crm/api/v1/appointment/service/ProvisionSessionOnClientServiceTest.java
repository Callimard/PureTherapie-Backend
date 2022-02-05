package puretherapie.crm.api.v1.appointment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.product.aesthetic.bundle.service.ReduceStockService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.UseSessionService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.api.v1.waitingroom.service.RemoveFromWaitingRoomService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.appointment.service.ProvisionSessionOnClientService.*;

@SpringBootTest
@DisplayName("TerminateClientService tests")
public class ProvisionSessionOnClientServiceTest {

    @BeforeEach
    void setUp() {
        prepareSPRepository();
        prepareBPRepository();
        prepareStockRepository();
        prepareUseSessionService();
        prepareReduceStockServiceSuccess();
        prepareAppointmentAC();
        prepareStock();
    }

    @Autowired
    private ProvisionSessionOnClientService tcs;

    @Nested
    @DisplayName("Terminate client with appointment tests")
    class TerminateWithAppointment {

        @Test
        @DisplayName("Test with not found client fail")
        void testWithNotFoundClient() {
            prepareClientRepository();

            SimpleResponseDTO res = tcs.provisionWithAppointment(590);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with client not in waiting room fail")
        void testWithClientNotInWaitingRoom() {
            prepareClientRepository();
            prepareClientNotInWR();

            SimpleResponseDTO res = tcs.provisionWithAppointment(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_IN_WR_ERROR);
        }

        @Test
        @DisplayName("Test with client without appointment fail")
        void testWithClientWithoutAppointment() {
            prepareClientRepository();
            prepareClientInWR();
            prepareClientWithoutAppointment();

            SimpleResponseDTO res = tcs.provisionWithAppointment(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_WITHOUT_APPOINTMENT_ERROR);
        }

        @Test
        @DisplayName("Test with canceled appointment fail")
        void testWithCanceledAppointment() {
            prepareClientRepository();
            prepareClientInWR();
            prepareClientWithAppointment();
            prepareAppointmentCanceled();

            SimpleResponseDTO res = tcs.provisionWithAppointment(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_APPOINTMENT_CANCELED_ERROR);
        }

        @Test
        @DisplayName("Test with fail to remove client from waiting room")
        void testWithFailToRemoveFromWR() {
            prepareClientRepository();
            prepareClient();
            prepareClientInWR();
            prepareClientWithAppointment();
            prepareAppointmentNotCanceled();
            prepareFailRemoveWR();

            SimpleResponseDTO res = tcs.provisionWithAppointment(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, FAIL_TO_REMOVE_CLIENT_WR_ERROR);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testWithAllCorrect() {
            prepareClientRepository();
            prepareClient();
            prepareClientInWR();
            prepareClientWithAppointment();
            prepareAppointmentNotCanceled();
            prepareSuccessRemoveWR();

            SimpleResponseDTO res = tcs.provisionWithAppointment(CLIENT_ID);
            verifySuccess(res);
        }
    }

    @Nested
    @DisplayName("Terminate client without appointment tests")
    class TerminateWithoutAppointment {
        @Test
        @DisplayName("Test with not found client fail")
        void testWithNotFoundClient() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(215, TECHNICIAN_ID, AC_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found technician fail")
        void testWithNotFoundTechnician() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(CLIENT_ID, 545, AC_ID);
            verifyFail(res);
            verifyFailType(res, TECHNICIAN_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found aesthetic care fail")
        void testWithNotFoundAestheticCare() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(CLIENT_ID, TECHNICIAN_ID, 302);
            verifyFail(res);
            verifyFailType(res, AESTHETIC_CARE_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with client not in waiting room fail")
        void testWithClientNotInWaitingRoom() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();
            prepareClientNotInWR();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_IN_WR_ERROR);
        }

        @Test
        @DisplayName("Test with fail to remove client from waiting room")
        void testWithFailToRemoveFromWR() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();
            prepareClient();
            prepareClientInWR();
            prepareClientWithAppointment();
            prepareAppointmentNotCanceled();
            prepareFailRemoveWR();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID);
            verifyFail(res);
            verifyFailType(res, FAIL_TO_REMOVE_CLIENT_WR_ERROR);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testWithAllCorrect() {
            prepareClientRepository();
            prepareTechnicianRepository();
            prepareACRepository();
            prepareClient();
            prepareClientInWR();
            prepareClientWithAppointment();
            prepareAppointmentNotCanceled();
            prepareSuccessRemoveWR();

            SimpleResponseDTO res = tcs.provisionWithoutAppointment(CLIENT_ID, TECHNICIAN_ID, AC_ID);
            verifySuccess(res);
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

    void verifyFailType(SimpleResponseDTO res, String expectedKey) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
        assertThat(res.message()).isEqualTo(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 629;

    @MockBean
    private TechnicianRepository mockTechnicianRepository;
    @Mock
    private Technician mockTechnician;
    private static final int TECHNICIAN_ID = 146;

    @MockBean
    private AestheticCareRepository mockACRepository;
    @Mock
    private AestheticCare mockAC;
    private static final int AC_ID = 434;

    @MockBean
    private WaitingRoomRepository mockWRRepository;
    @Mock
    private WaitingRoom mockWR;
    @Mock
    private Appointment mockAppointment;

    @MockBean
    private RemoveFromWaitingRoomService mockRWRService;

    @MockBean
    private AestheticCareProvisionRepository mockACPRepository;

    @MockBean
    private SessionPurchaseRepository mockSPRepository;
    @Mock
    private SessionPurchase mockSP;

    @MockBean
    private UseSessionService mockUSService;

    @MockBean
    private BundlePurchaseRepository mockBPRepository;
    @Mock
    private BundlePurchase mockBP;

    @MockBean
    private StockRepository mockStockRepository;
    @Mock
    private Stock mockStock;

    @MockBean
    private ReduceStockService mockReduceStockService;

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareClient() {
        given(mockClient.getIdPerson()).willReturn(CLIENT_ID);
    }

    private void prepareTechnicianRepository() {
        given(mockTechnicianRepository.findByIdPerson(TECHNICIAN_ID)).willReturn(mockTechnician);
    }

    private void prepareACRepository() {
        given(mockACRepository.findByIdAestheticCare(AC_ID)).willReturn(mockAC);
    }

    private void prepareClientInWR() {
        given(mockWRRepository.findByClient(mockClient)).willReturn(mockWR);
        given(mockWR.getClient()).willReturn(mockClient);
    }

    private void prepareClientNotInWR() {
        given(mockWRRepository.findByClient(mockClient)).willReturn(null);
    }

    private void prepareClientWithAppointment() {
        given(mockWR.getAppointment()).willReturn(mockAppointment);
    }

    private void prepareClientWithoutAppointment() {
        given(mockWR.getAppointment()).willReturn(null);
    }

    private void prepareAppointmentNotCanceled() {
        given(mockAppointment.isCanceled()).willReturn(false);
    }

    private void prepareAppointmentCanceled() {
        given(mockAppointment.isCanceled()).willReturn(true);
    }

    private void prepareSuccessRemoveWR() {
        given(mockRWRService.removeClient(anyInt())).willReturn(SimpleResponseDTO.generateSuccess(""));
    }

    private void prepareFailRemoveWR() {
        given(mockRWRService.removeClient(anyInt())).willReturn(SimpleResponseDTO.generateFail(""));
    }

    private void prepareAppointmentAC() {
        given(mockAppointment.getAestheticCare()).willReturn(mockAC);
    }

    private void prepareSPRepository() {
        List<SessionPurchase> list = new ArrayList<>();
        list.add(mockSP);
        given(mockSPRepository.findByClientAndAestheticCare(mockClient, mockAC)).willReturn(list);
    }

    private void prepareBPRepository() {
        List<BundlePurchase> list = new ArrayList<>();
        list.add(mockBP);
        given(mockBPRepository.findByClient(mockClient)).willReturn(list);
    }

    private void prepareStockRepository() {
        given(mockStockRepository.findByBundlePurchaseAndAestheticCare(mockBP, mockAC)).willReturn(mockStock);
    }

    private void prepareStock() {
        given(mockStock.hasRemainingQuantity()).willReturn(true);
    }

    private void prepareUseSessionService() {
        given(mockUSService.useSession(anyInt())).willReturn(SimpleResponseDTO.generateSuccess(""));
    }

    private void prepareReduceStockServiceSuccess() {
        given(mockReduceStockService.hasSuccess(any())).willReturn(true);
    }

}
