package puretherapie.crm.api.v1.product.aesthetic.care.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.PaymentType;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.PaymentTypeRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.product.aesthetic.care.service.PurchaseSessionService.*;

@SpringBootTest
public class PurchaseSessionServiceTest {

    @Autowired
    private PurchaseSessionService pss;

    @BeforeEach
    void setUp() {
        prepareMinimalRepository();
    }

    @Nested
    @DisplayName("Session purchase tests")
    class PurchaseSession {

        @Test
        @DisplayName("Test with not found client fail")
        void testWithNotFoundClient() {
            SimpleResponseDTO res = pss.purchaseSession(-1, AC_ID, -1, PAYMENT_TYPE_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found AC fail")
        void testWithNotFoundAC() {
            SimpleResponseDTO res = pss.purchaseSession(CLIENT_ID, -1, -1, PAYMENT_TYPE_ID);
            verifyFail(res);
            verifyFailType(res, AESTHETIC_CARE_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with not found payment type fail")
        void testWithNotFoundPaymentType() {
            SimpleResponseDTO res = pss.purchaseSession(CLIENT_ID, AC_ID, -1, -1);
            verifyFail(res);
            verifyFailType(res, PAYMENT_TYPE_NOT_FOUND);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testWithAllCorrect() {
            SimpleResponseDTO res = pss.purchaseSession(CLIENT_ID, AC_ID, 2.0, PAYMENT_TYPE_ID);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with negative custom price success")
        void testWithNegativeCustomPriceSuccess() {
            SimpleResponseDTO res = pss.purchaseSession(CLIENT_ID, AC_ID, -1, PAYMENT_TYPE_ID);
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
        assertThat(res.message()).isEqualTo(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 939;

    @MockBean
    private AestheticCareRepository mockACRepository;
    @Mock
    private AestheticCare mockAC;
    private static final int AC_ID = 561;

    @MockBean
    private PaymentTypeRepository mockPaymentTypeRepository;
    @Mock
    private PaymentType mockPaymentType;
    private static final int PAYMENT_TYPE_ID = 160;

    @MockBean
    private BillRepository mockBillRepository;
    @Mock
    private Bill mockBill;

    @MockBean
    private SessionPurchaseRepository mockSPRepository;

    private void prepareMinimalRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
        given(mockACRepository.findByIdAestheticCare(AC_ID)).willReturn(mockAC);
        given(mockPaymentTypeRepository.findByIdPaymentType(PAYMENT_TYPE_ID)).willReturn(mockPaymentType);
    }

}
