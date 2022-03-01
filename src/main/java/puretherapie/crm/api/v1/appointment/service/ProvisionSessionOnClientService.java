package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.api.v1.product.aesthetic.bundle.service.ReduceStockService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.PurchaseSessionService;
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
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.data.notification.NotificationLevel.BOSS_LEVEL;

@Slf4j
@AllArgsConstructor
@Service
public class ProvisionSessionOnClientService {

    // Constants.

    private static final String PROVISION_SESSION_TITLE = "Soin pratiqué";
    private static final String PROVISION_SESSION_TEXT = "Le soin %s a été pratiqué sur le client %s par %s";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String TECHNICIAN_ID_NOT_FOUND_ERROR = "technician_id_not_found";
    public static final String AESTHETIC_CARE_ID_NOT_FOUND_ERROR = "aesthetic_care_id_not_found";
    public static final String CLIENT_NOT_IN_WR_ERROR = "client_not_in_wr_error";
    public static final String CLIENT_WITHOUT_APPOINTMENT_ERROR = "client_without_appointment_error";
    public static final String CLIENT_APPOINTMENT_CANCELED_ERROR = "client_appointment_canceled_error";
    public static final String FAIL_TO_REMOVE_CLIENT_WR_ERROR = "fail_remove_client_from_wr_error";
    public static final String CANNOT_USE_SESSION_PURCHASE_AFTER_UPDATE_STOCK_ERROR = "cannot_use_session_purchase_after_update_stock_error";
    public static final String AC_PROVISION_ALREADY_DONE_ON_CLIENT_FOR_APPOINTMENT_ERROR =
            "ac_provision_already_done_on_client_for_appointment_error";

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
    private final PurchaseSessionService purchaseSessionService;
    private final NotificationCreationService notificationCreationService;

    // Methods.

    /**
     * Provision the client. The client must be in the waiting room and had an appointment.
     *
     * @param idClient id client to terminate
     *
     * @return the res of the try of terminate the client
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleResponseDTO provisionWithAppointment(int idClient) {
        try {
            Client client = verifyClient(idClient);
            WaitingRoom waitingRoom = verifyIsInWaitingRoom(client);
            Appointment appointment = waitingRoom.getAppointment();
            verifyAppointment(appointment);
            removeFromWaitingRoom(waitingRoom);
            saveAestheticCareProvision(client, appointment, appointment.getTechnician(), appointment.getAestheticCare());
            updateClientACStock(client, appointment.getAestheticCare());
            notifyProvisionSession(appointment.getAestheticCare(), client, appointment.getTechnician());
            return SimpleResponseDTO.generateSuccess("Success to provision client");
        } catch (Exception e) {
            log.debug("Fail to provision the client with appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleResponseDTO provisionWithoutAppointment(int idClient, int idTechnician, int idAestheticCare) {
        try {
            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            WaitingRoom waitingRoom = verifyIsInWaitingRoom(client);
            removeFromWaitingRoom(waitingRoom);
            saveAestheticCareProvision(client, null, technician, aestheticCare);
            updateClientACStock(client, aestheticCare);
            return SimpleResponseDTO.generateSuccess("Success to terminate client");
        } catch (Exception e) {
            log.debug("Fail to provision the client without appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ProvisionSessionOnClientException(CLIENT_NOT_FOUND_ERROR);
        return client;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null)
            throw new ProvisionSessionOnClientException(TECHNICIAN_ID_NOT_FOUND_ERROR);
        return t;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new ProvisionSessionOnClientException(AESTHETIC_CARE_ID_NOT_FOUND_ERROR);
        return ac;
    }

    private WaitingRoom verifyIsInWaitingRoom(Client client) {
        WaitingRoom waitingRoom = waitingRoomRepository.findByClient(client);
        if (waitingRoom == null)
            throw new ProvisionSessionOnClientException(CLIENT_NOT_IN_WR_ERROR);
        return waitingRoom;
    }

    private void verifyAppointment(Appointment appointment) {
        if (appointment == null)
            throw new ProvisionSessionOnClientException(CLIENT_WITHOUT_APPOINTMENT_ERROR);

        if (appointment.isCanceled())
            throw new ProvisionSessionOnClientException(CLIENT_APPOINTMENT_CANCELED_ERROR);
    }

    private void removeFromWaitingRoom(WaitingRoom waitingRoom) {
        SimpleResponseDTO res = removeFromWaitingRoomService.removeClient(waitingRoom.getClient().getIdPerson());
        if (!res.success())
            throw new ProvisionSessionOnClientException(FAIL_TO_REMOVE_CLIENT_WR_ERROR);
    }

    private void saveAestheticCareProvision(Client client, Appointment appointment, Technician technician, AestheticCare aestheticCare) {
        AestheticCareProvision acProvision = buildACProvision(client, appointment, technician, aestheticCare);

        if (appointment != null) {
            AestheticCareProvision acProvisionDoneForAppointment = aestheticCareProvisionRepository.findByClientAndAppointment(client, appointment);
            if (acProvisionDoneForAppointment != null)
                throw new ProvisionSessionOnClientException(AC_PROVISION_ALREADY_DONE_ON_CLIENT_FOR_APPOINTMENT_ERROR);
        }
        acProvision = aestheticCareProvisionRepository.save(acProvision);
        log.info("Save ACProvision => {}", acProvision);
    }

    private AestheticCareProvision buildACProvision(Client client, Appointment appointment, Technician technician, AestheticCare aestheticCare) {
        return AestheticCareProvision.builder()
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .appointment(appointment)
                .date(LocalDateTime.now())
                .build();
    }

    private void updateClientACStock(Client client, AestheticCare aestheticCare) {
        if (!tryToUseSessionPurchase(client, aestheticCare) && !tryToReduceBundlePurchaseStock(client, aestheticCare)) {
            purchaseSessionService.purchaseSession(client.getIdPerson(), aestheticCare.getIdAestheticCare());
            boolean usedSession = tryToUseSessionPurchase(client, aestheticCare);
            if (!usedSession)
                throw new ProvisionSessionOnClientException(CANNOT_USE_SESSION_PURCHASE_AFTER_UPDATE_STOCK_ERROR);
        }
    }

    private boolean tryToUseSessionPurchase(Client client, AestheticCare aestheticCare) {
        List<SessionPurchase> clientSessionPurchases = sessionPurchaseRepository.findByClientAndAestheticCare(client, aestheticCare);

        if (clientSessionPurchases != null && !clientSessionPurchases.isEmpty())
            for (SessionPurchase sessionPurchase : clientSessionPurchases) {
                if (!sessionPurchase.isUsed()) {
                    log.info("Found session purchase not used for the client {} and the aesthetic care {}", client.simplyIdentifier(), aestheticCare);
                    SimpleResponseDTO reduceRes = useSessionService.useSession(sessionPurchase.getIdSessionPurchase());
                    if (!reduceRes.success()) {
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

    private void notifyProvisionSession(AestheticCare aestheticCare, Client client, Technician technician) {
        boolean success = notificationCreationService.createNotification(PROVISION_SESSION_TITLE,
                                                                         PROVISION_SESSION_TEXT.formatted(aestheticCare.getName(),
                                                                                                          client.simplyIdentifier(),
                                                                                                          technician.simplyIdentifier()),
                                                                         BOSS_LEVEL, true);
        if (!success)
            log.error("Fail to create provision session notification");
    }

    // Exceptions.

    private static class ProvisionSessionOnClientException extends RuntimeException {
        public ProvisionSessionOnClientException(String message) {
            super(message);
        }
    }
}
