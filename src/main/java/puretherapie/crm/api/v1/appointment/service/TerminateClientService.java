package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.api.v1.product.aesthetic.bundle.service.ReduceStockService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.UseSessionService;
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
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class TerminateClientService extends SimpleService {

    // Constants.

    public static final String TERMINATE_CLIENT_SUCCESS = "terminate_client_success";
    public static final String TERMINATE_CLIENT_FAIL = "terminate_client_fail";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String TECHNICIAN_ID_NOT_FOUND_ERROR = "technician_id_not_found";
    public static final String AESTHETIC_CARE_ID_NOT_FOUND_ERROR = "aesthetic_care_id_not_found";
    public static final String CLIENT_NOT_IN_WR_ERROR = "client_not_in_wr_error";
    public static final String CLIENT_WITHOUT_APPOINTMENT_ERROR = "client_without_appointment_error";
    public static final String CLIENT_APPOINTMENT_CANCELED_ERROR = "client_appointment_canceled_error";
    public static final String FAIL_TO_REMOVE_CLIENT_WR_ERROR = "fail_remove_client_from_wr_error";
    public static final String NOT_STOCK_ERROR = "no_stock_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final WaitingRoomRepository waitingRoomRepository;
    private final RemoveFromWaitingRoomService removeFromWaitingRoomService;
    private final AestheticCareProvisionRepository aestheticCareProvisionRepository;
    private final SessionPurchaseRepository sessionPurchaseRepository;
    private final UseSessionService useSessionService;
    private final BundlePurchaseRepository bundlePurchaseRepository;
    private final StockRepository stockRepository;
    private final ReduceStockService reduceStockService;

    // Methods.

    /**
     * Try to terminate the client. The client must be in the waiting room and had an appointment.
     *
     * @param idClient id client to terminate
     *
     * @return the res of the try of terminate the client
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> terminateWithAppointment(int idClient) {
        try {
            Client client = verifyClient(idClient);
            WaitingRoom waitingRoom = verifyIsInWaitingRoom(client);
            Appointment appointment = waitingRoom.getAppointment();
            verifyAppointment(appointment);
            removeFromWaitingRoom(waitingRoom);
            saveAestheticCareProvision(client, appointment, appointment.getTechnician(), appointment.getAestheticCare());
            updateClientACStock(client, appointment.getAestheticCare());
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to terminate the client with appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> terminateWithoutAppointment(int idClient, int idTechnician, int idAestheticCare) {
        try {
            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            WaitingRoom waitingRoom = verifyIsInWaitingRoom(client);
            removeFromWaitingRoom(waitingRoom);
            saveAestheticCareProvision(client, null, technician, aestheticCare);
            updateClientACStock(client, aestheticCare);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to terminate the client without appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new TerminateClientException("Client not found", generateError(CLIENT_NOT_FOUND_ERROR,
                                                                                 "Client with id %s not found".formatted(idClient)));
        return client;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null)
            throw new TerminateClientException("Not find technician for idTechnician %s".formatted(idTechnician),
                                               generateError(TECHNICIAN_ID_NOT_FOUND_ERROR, "Technician id not found"));
        return t;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new TerminateClientException("Not find aesthetic care for idAestheticCare %s".formatted(idAestheticCare),
                                               generateError(AESTHETIC_CARE_ID_NOT_FOUND_ERROR, "Aesthetic care id not found"));
        return ac;
    }

    private WaitingRoom verifyIsInWaitingRoom(Client client) {
        WaitingRoom waitingRoom = waitingRoomRepository.findByClient(client);
        if (waitingRoom == null)
            throw new TerminateClientException("Client not in waiting room", generateError(CLIENT_NOT_IN_WR_ERROR, "Client not in WR"));
        return waitingRoom;
    }

    private void verifyAppointment(Appointment appointment) {
        if (appointment == null)
            throw new TerminateClientException("Client without appointment, missing information to create AC Provision",
                                               generateError(CLIENT_WITHOUT_APPOINTMENT_ERROR, "Client with no appointment"));

        if (appointment.isCanceled())
            throw new TerminateClientException("Client appointment canceled", generateError(CLIENT_APPOINTMENT_CANCELED_ERROR, "Client appointment " +
                    "canceled"));
    }

    private void removeFromWaitingRoom(WaitingRoom waitingRoom) {
        Map<String, Object> res = removeFromWaitingRoomService.removeClient(waitingRoom.getClient().getIdPerson());
        if (!removeFromWaitingRoomService.hasSuccess(res))
            throw new TerminateClientException("Fail to remove client from waiting room", generateError(FAIL_TO_REMOVE_CLIENT_WR_ERROR, "Fail to " +
                    "remove client from waiting room"));
    }

    private void saveAestheticCareProvision(Client client, Appointment appointment, Technician technician, AestheticCare aestheticCare) {
        AestheticCareProvision acProvision = buildACProvision(client, appointment, technician, aestheticCare);
        acProvision = aestheticCareProvisionRepository.save(acProvision);
        log.debug("Save ACProvision {}", acProvision);
    }

    private AestheticCareProvision buildACProvision(Client client, Appointment appointment, Technician technician, AestheticCare aestheticCare) {
        return AestheticCareProvision.builder()
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .appointment(appointment)
                .date(OffsetDateTime.now())
                .build();
    }

    private void updateClientACStock(Client client, AestheticCare aestheticCare) {
        if (!tryToUseSessionPurchase(client, aestheticCare) && !tryToReduceBundlePurchaseStock(client, aestheticCare))
            throw new TerminateClientException("Client have not stock for the aesthetic care", generateError(NOT_STOCK_ERROR, "Client has not " +
                    "stock"));
    }

    private boolean tryToUseSessionPurchase(Client client, AestheticCare aestheticCare) {
        List<SessionPurchase> clientSessionPurchases = sessionPurchaseRepository.findByClientAndAestheticCare(client, aestheticCare);

        if (clientSessionPurchases != null && !clientSessionPurchases.isEmpty())
            for (SessionPurchase sessionPurchase : clientSessionPurchases) {
                if (!sessionPurchase.isUsed()) {
                    log.info("Found session purchase not used for the client {} and the aesthetic care {}", client.simplyIdentifier(), aestheticCare);
                    Map<String, Object> reduceRes = useSessionService.useSession(sessionPurchase.getIdSessionPurchase());
                    if (!useSessionService.hasSuccess(reduceRes)) {
                        log.error("Fail to reduce stock of session purchase {}", sessionPurchase);
                        continue;
                    }

                    return true;
                }
            }
        else
            log.info("Not found session purchase for the client {} and the aesthetic care {}", client.simplyIdentifier(), aestheticCare);

        return false;
    }

    private boolean tryToReduceBundlePurchaseStock(Client client, AestheticCare aestheticCare) {
        List<BundlePurchase> clientBundlePurchases = bundlePurchaseRepository.findByClient(client);

        if (clientBundlePurchases != null && !clientBundlePurchases.isEmpty()) {
            for (BundlePurchase bundlePurchase : clientBundlePurchases) {
                Stock stock = stockRepository.findByBundlePurchaseAndAestheticCare(bundlePurchase, aestheticCare);
                if (stock != null && stock.hasRemainingQuantity()) {
                    log.info("Found bundle purchase with remaining stock for the client {} and the aesthetic care {}", client.simplyIdentifier(),
                             aestheticCare);
                    Map<String, Object> reduceStock = reduceStockService.reduceStock(stock.getIdStock(), 1);
                    if (!reduceStockService.hasSuccess(reduceStock)) {
                        log.error("Fail to reduce stock of bundle purchase {}", bundlePurchase);
                        continue;
                    }

                    return true;
                }
            }
            log.info("Not found bundle with stock for the client {} and the aesthetic care {}", client.simplyIdentifier(), aestheticCare);
        } else
            log.info("Not found bundle purchase for the client {}", client);

        return false;
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return TERMINATE_CLIENT_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return TERMINATE_CLIENT_FAIL;
    }

    // Exceptions.

    private static class TerminateClientException extends SimpleService.ServiceException {
        public TerminateClientException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
