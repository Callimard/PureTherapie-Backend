package puretherapie.crm.api.v1.product.aesthetic.bundle.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundleRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.PaymentType;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.PaymentTypeRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.product.aesthetic.bundle.service.BundlePurchaseService.*;

@SpringBootTest
@DisplayName("BundlePurchaseService tests")
public class BundlePurchaseServiceTest {

    @Autowired
    private BundlePurchaseService bps;

    // Tests.

    @BeforeEach
    void setUp() {
        prepareMinimalRepository();
        prepareACP();
    }

    @Nested
    @DisplayName("Bundle purchase tests")
    class BundlePurchase {

        @Test
        @DisplayName("Test with client not found fail")
        void testWithClientNotFound() {
            Map<String, Object> res = bps.purchaseBundle(-1, BUNDLE_ID, Collections.emptySet(), -1, PAYMENT_TYPE_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with bundle not found fail")
        void testWithBundleNotFound() {
            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, -1, Collections.emptySet(), -1, PAYMENT_TYPE_ID);
            verifyFail(res);
            verifyFailType(res, BUNDLE_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with empty bundle fail")
        void testWithEmptyBundle() {
            prepareEmptyBundle();

            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, Collections.emptySet(), -1, PAYMENT_TYPE_ID);
            verifyFail(res);
            verifyFailType(res, EMPTY_BUNDLE_ERROR);
        }

        @Test
        @DisplayName("Test with payment type not found fail")
        void testWithPaymentTypeNotFound() {
            prepareNotEmptyBundle();

            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, Collections.emptySet(), -1, -1);
            verifyFail(res);
            verifyFailType(res, PAYMENT_TYPE_NOT_FOUND);
        }

        @Test
        @DisplayName("Test with no customizations success")
        void testWithNoCustomizations() {
            prepareNotEmptyBundle();

            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, Collections.emptySet(), -1, PAYMENT_TYPE_ID);
            verifySuccess(res);

            res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, null, -1, PAYMENT_TYPE_ID);
            verifySuccess(res);

            res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, null, 68, PAYMENT_TYPE_ID);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with no corresponding customizations")
        void testWithNoCorrespondingCustomizations() {
            prepareNotEmptyBundle();

            Set<ACPackageCustomization> setCustomisation = new HashSet<>();
            setCustomisation.add(new ACPackageCustomization(178, 9));

            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, setCustomisation, -1, PAYMENT_TYPE_ID);
            verifySuccess(res);

            res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, setCustomisation, 75, PAYMENT_TYPE_ID);
            verifySuccess(res);
        }

        @Test
        @DisplayName("Test with corresponding customizations")
        void testWithCorrespondingCustomizations() {
            prepareNotEmptyBundle();

            Set<ACPackageCustomization> setCustomisation = new HashSet<>();
            setCustomisation.add(new ACPackageCustomization(ACP_ID, 9));

            Map<String, Object> res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, setCustomisation, -1, PAYMENT_TYPE_ID);
            verifySuccess(res);

            res = bps.purchaseBundle(CLIENT_ID, BUNDLE_ID, setCustomisation, 50, PAYMENT_TYPE_ID);
            verifySuccess(res);
        }

    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(bps.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(bps.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(bps.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 831;

    @MockBean
    private BundleRepository mockBundleRepository;
    @Mock
    private Bundle mockBundle;
    private static final int BUNDLE_ID = 318;
    @Mock
    private AestheticCarePackage mockACP;
    private static final int ACP_ID = 196;

    @MockBean
    private PaymentTypeRepository mockPaymentTypeRepository;
    @Mock
    private PaymentType mockPaymentType;
    private static final int PAYMENT_TYPE_ID = 931;

    @MockBean
    private BillRepository mockBillRepository;
    @Mock
    private Bill mockBill;

    @MockBean
    private BundlePurchaseRepository mockBundlePurchaseRepository;
    @Mock
    private BundlePurchase mockBundlePurchase;

    @MockBean
    private StockRepository mockStockRepository;
    @Mock
    private Stock mockStock1, mockStock2;

    private void prepareMinimalRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
        given(mockBundleRepository.findByIdBundle(BUNDLE_ID)).willReturn(mockBundle);
        given(mockPaymentTypeRepository.findByIdPaymentType(PAYMENT_TYPE_ID)).willReturn(mockPaymentType);
    }

    private void prepareEmptyBundle() {
        given(mockBundle.getAestheticCarePackages()).willReturn(Collections.emptyList());
    }

    private void prepareNotEmptyBundle() {
        List<AestheticCarePackage> listAC = new ArrayList<>();
        listAC.add(mockACP);
        given(mockBundle.getAestheticCarePackages()).willReturn(listAC);
    }

    private void prepareACP() {
        given(mockACP.getIdAestheticCarePackage()).willReturn(ACP_ID);
    }

}
