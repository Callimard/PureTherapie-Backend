package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class UseSessionService extends SimpleService {

    // Constants.

    public static final String USE_SESSION_SUCCESS = "use_session_success";
    public static final String USE_SESSION_FAIL = "use_session_fail";

    public static final String SESSION_PURCHASE_NOT_FOUND_ERROR = "sp_not_found_error";
    public static final String SESSION_ALREADY_USED_ERROR = "sp_already_used_error";

    // Variables.

    private final SessionPurchaseRepository sessionPurchaseRepository;

    // Methods.

    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<String, Object> useSession(int idSessionPurchase) {
        try {
            SessionPurchase sessionPurchase = verifySessionPurchase(idSessionPurchase);
            verifyNotUsed(sessionPurchase);
            updateSessionPurchase(sessionPurchase);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to use session, error message {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private SessionPurchase verifySessionPurchase(int idSessionPurchase) {
        SessionPurchase sessionPurchase = sessionPurchaseRepository.findByIdSessionPurchase(idSessionPurchase);
        if (sessionPurchase == null)
            throw new UseSessionException("Session purchase not found", generateError(SESSION_PURCHASE_NOT_FOUND_ERROR, "SP not found"));

        return sessionPurchase;
    }

    private void verifyNotUsed(SessionPurchase sessionPurchase) {
        if (sessionPurchase.isUsed())
            throw new UseSessionException("Session already used", generateError(SESSION_ALREADY_USED_ERROR, "Session already used"));
    }

    private void updateSessionPurchase(SessionPurchase sessionPurchase) {
        sessionPurchase.setUsed(true);
        sessionPurchase = sessionPurchaseRepository.save(sessionPurchase);
        log.debug("Update (set to used = true) session purchase {}", sessionPurchase);
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return USE_SESSION_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return USE_SESSION_FAIL;
    }

    // Exceptions.

    private static class UseSessionException extends SimpleService.ServiceException {
        public UseSessionException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
