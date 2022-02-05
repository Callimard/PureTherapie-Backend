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
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

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
            SimpleResponseDTO res = uss.useSession(-1);
            verifyFail(res);
            verifyFailType(res, SESSION_PURCHASE_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with session already used fail")
        void testWithSessionAlreadyUsed() {
            prepareUsedSession();

            SimpleResponseDTO res = uss.useSession(SP_ID);
            verifyFail(res);
            verifyFailType(res, SESSION_ALREADY_USED_ERROR);
        }

        @Test
        @DisplayName("Test with session not used success")
        void testWithSessionNotUsed() {
            prepareNotUsedSession();

            SimpleResponseDTO res = uss.useSession(SP_ID);
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
