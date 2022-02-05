package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

@Slf4j
@AllArgsConstructor
@Service
public class UseSessionService {

    // Constants.

    public static final String USE_SESSION_SUCCESS = "use_session_success";
    public static final String USE_SESSION_FAIL = "use_session_fail";

    public static final String SESSION_PURCHASE_NOT_FOUND_ERROR = "sp_not_found_error";
    public static final String SESSION_ALREADY_USED_ERROR = "sp_already_used_error";

    // Variables.

    private final SessionPurchaseRepository sessionPurchaseRepository;

    // Methods.

    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO useSession(int idSessionPurchase) {
        try {
            SessionPurchase sessionPurchase = verifySessionPurchase(idSessionPurchase);
            verifyNotUsed(sessionPurchase);
            updateSessionPurchase(sessionPurchase);
            return SimpleResponseDTO.generateSuccess("Success to use session.");
        } catch (Exception e) {
            log.debug("Fail to use session, error message {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private SessionPurchase verifySessionPurchase(int idSessionPurchase) {
        SessionPurchase sessionPurchase = sessionPurchaseRepository.findByIdSessionPurchase(idSessionPurchase);
        if (sessionPurchase == null)
            throw new UseSessionException(SESSION_PURCHASE_NOT_FOUND_ERROR);

        return sessionPurchase;
    }

    private void verifyNotUsed(SessionPurchase sessionPurchase) {
        if (sessionPurchase.isUsed())
            throw new UseSessionException(SESSION_ALREADY_USED_ERROR);
    }

    private void updateSessionPurchase(SessionPurchase sessionPurchase) {
        sessionPurchase.setUsed(true);
        sessionPurchase = sessionPurchaseRepository.save(sessionPurchase);
        log.debug("Update (set to used = true) session purchase {}", sessionPurchase);
    }

    // Exceptions.

    private static class UseSessionException extends RuntimeException {
        public UseSessionException(String message) {
            super(message);
        }
    }
}
