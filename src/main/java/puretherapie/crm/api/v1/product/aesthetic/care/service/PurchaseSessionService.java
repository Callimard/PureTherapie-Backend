package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.PaymentType;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.PaymentTypeRepository;

import java.time.OffsetDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class PurchaseSessionService {

    // Constants.

    public static final String SESSION_PURCHASE_SUCCESS = "session_purchase_success";
    public static final String SESSION_PURCHASE_FAIL = "session_purchase_fail";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String AESTHETIC_CARE_NOT_FOUND_ERROR = "ac_not_found_error";
    public static final String PAYMENT_TYPE_NOT_FOUND = "payment_type_not_found_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final BillRepository billRepository;
    private final SessionPurchaseRepository sessionPurchaseRepository;

    // Methods.

    /**
     * @param idClient        the id of the client
     * @param idAestheticCare the id of the aesthetic care
     * @param customPrice     the custom price (ignored if negative)
     * @param idPaymentType   this idPaymentType
     *
     * @return the res of the purchaseSession
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO purchaseSession(int idClient, int idAestheticCare, double customPrice, int idPaymentType) {
        try {
            Client client = verifyClient(idClient);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            PaymentType paymentType = verifyPaymentType(idPaymentType);
            Bill bill = saveBill(client, paymentType, aestheticCare.getPrice(), customPrice);
            saveSessionPurchase(client, aestheticCare, bill);
            return generateSuccessRes(client.simplyIdentifier(), aestheticCare.getName());
        } catch (Exception e) {
            log.error("Fail to purchase a session, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e.getMessage());
        }
    }

    private SimpleResponseDTO generateSuccessRes(String clientIdentifier, String acName) {
        return SimpleResponseDTO.generateSuccess("Success purchase session of AC %s for the client %s".formatted(acName, clientIdentifier));
    }

    private SimpleResponseDTO generateErrorRes(String errorMsg) {
        return SimpleResponseDTO.generateFail(errorMsg);
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new PurchaseSessionException(CLIENT_NOT_FOUND_ERROR);
        return client;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new PurchaseSessionException(AESTHETIC_CARE_NOT_FOUND_ERROR);
        return ac;
    }

    private PaymentType verifyPaymentType(int idPaymentType) {
        PaymentType paymentType = paymentTypeRepository.findByIdPaymentType(idPaymentType);
        if (paymentType == null)
            throw new PurchaseSessionException(PAYMENT_TYPE_NOT_FOUND);
        return paymentType;
    }

    private Bill saveBill(Client client, PaymentType paymentType, Double basePrice, double customPrice) {
        Bill bill = buildBill(client, paymentType, basePrice, customPrice);
        bill = billRepository.save(bill);
        log.info("Save bill {}", bill);
        return bill;
    }

    private Bill buildBill(Client client, PaymentType paymentType, double basePrice, double customPrice) {
        return Bill.builder()
                .client(client)
                .paymentType(paymentType)
                .basePrice(basePrice)
                .purchasePrice(customPrice < 0 ? basePrice : customPrice)
                .creationDate(OffsetDateTime.now())
                .build();
    }

    private void saveSessionPurchase(Client client, AestheticCare aestheticCare, Bill bill) {
        SessionPurchase sessionPurchase = buildSessionPurchase(client, aestheticCare, bill);
        sessionPurchase = sessionPurchaseRepository.save(sessionPurchase);
        log.debug("Save session purchase {}", sessionPurchase);
    }

    private SessionPurchase buildSessionPurchase(Client client, AestheticCare aestheticCare, Bill bill) {
        return SessionPurchase.builder()
                .used(false)
                .client(client)
                .aestheticCare(aestheticCare)
                .bill(bill)
                .build();
    }

    // Exceptions.

    private static class PurchaseSessionException extends RuntimeException {
        public PurchaseSessionException(String message) {
            super(message);
        }
    }
}
