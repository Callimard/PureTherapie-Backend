package puretherapie.crm.api.v1.product.aesthetic.care.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.product.aesthetic.care.service.UseSessionService.SESSION_ALREADY_USED_ERROR;
import static puretherapie.crm.api.v1.product.aesthetic.care.service.UseSessionService.SESSION_PURCHASE_NOT_FOUND_ERROR;

@SpringBootTest
@DisplayName("UseSessionService tests")
public class UseSessionServiceTest {

    @Autowired
    private UseSessionService uss;

    @BeforeEach
    void setUp() {
        prepareMinimal();
    }

    @Nested
    @DisplayName("Use session tests")
    class UseSession {

        @Test
        @DisplayName("Test with session purchase not found fail")
        void testWithSessionPurchaseNotFound() {
            Map<String, Object> res = uss.useSession(-1);
            verifyFail(res);
            verifyFailType(res, SESSION_PURCHASE_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with session already used fail")
        void testWithSessionAlreadyUsed() {
            prepareUsedSession();

            Map<String, Object> res = uss.useSession(SP_ID);
            verifyFail(res);
            verifyFailType(res, SESSION_ALREADY_USED_ERROR);
        }

        @Test
        @DisplayName("Test with session not used success")
        void testWithSessionNotUsed() {
            prepareNotUsedSession();

            Map<String, Object> res = uss.useSession(SP_ID);
            verifySuccess(res);
        }
    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(uss.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(uss.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(uss.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private SessionPurchaseRepository mockSPRepository;
    @Mock
    private SessionPurchase mockSP;
    private static final int SP_ID = 45;

    private void prepareMinimal() {
        given(mockSPRepository.findByIdSessionPurchase(SP_ID)).willReturn(mockSP);
    }

    private void prepareNotUsedSession() {
        given(mockSP.isUsed()).willReturn(false);
    }

    private void prepareUsedSession() {
        given(mockSP.isUsed()).willReturn(true);
    }

}
