package puretherapie.crm.api.v1.product.aesthetic.care.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.SessionPurchaseDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.parameter.AestheticCareCreationParameter;
import puretherapie.crm.api.v1.product.aesthetic.care.service.AestheticCareService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.AestheticCareStockService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.PurchaseSessionService;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AestheticCareController.AESTHETIC_CARES_URL)
public class AestheticCareController {

    // Constants.

    public static final String AESTHETIC_CARES_URL = API_V1_URL + "/aesthetic_cares";

    public static final String AESTHETIC_CARE_PURCHASE = "/purchase";
    public static final String AESTHETIC_CARE_PURCHASE_URL = AESTHETIC_CARES_URL + AESTHETIC_CARE_PURCHASE;

    public static final String CLIENT_ALL_SESSION_PURCHASES = "/purchases";
    public static final String CLIENT_ALL_SESSION_PURCHASES_URL = AESTHETIC_CARES_URL + CLIENT_ALL_SESSION_PURCHASES;

    public static final String UNPAID_AESTHETIC_CARE_PURCHASES = CLIENT_ALL_SESSION_PURCHASES + "/unpaid";
    public static final String UNPAID_AESTHETIC_CARE_PURCHASES_URL = AESTHETIC_CARES_URL + UNPAID_AESTHETIC_CARE_PURCHASES;

    public static final String CLIENT_AC_STOCK = "/{idClient}/stock/{idAestheticCare}";
    public static final String CLIENT_AC_STOCK_URL = AESTHETIC_CARES_URL + CLIENT_AC_STOCK;

    // Variables.

    private final AestheticCareService aestheticCareService;
    private final SessionPurchaseRepository sessionPurchaseRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final PurchaseSessionService purchaseSessionService;
    private final PaymentService paymentService;
    private final AestheticCareStockService aestheticCareStockService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping
    public void createAestheticCare(@RequestBody AestheticCareCreationParameter aestheticCareCreationParameter) {
        aestheticCareService.createAestheticCare(aestheticCareCreationParameter.getName(), aestheticCareCreationParameter.getPrice(),
                                                 aestheticCareCreationParameter.getExecutionTime());
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_AC_STOCK)
    public ResponseEntity<Integer> getACStockOfClient(@PathVariable(name = "idClient") int idClient,
                                                      @PathVariable(name = "idAestheticCare") int idAestheticCare) {
        try {
            return ResponseEntity.ok(aestheticCareStockService.getACStockOfClient(idClient, idAestheticCare));
        } catch (Exception e) {
            log.error("Error during get the ac stock of client, Err msg = {}", e.getMessage());
            return ResponseEntity.badRequest().body(-1);
        }
    }

    @GetMapping
    public List<AestheticCareDTO> getAllAestheticCares() {
        List<AestheticCare> acList = this.aestheticCareRepository.findAll();
        List<AestheticCareDTO> allAC = new ArrayList<>();
        acList.forEach(ac -> allAC.add(ac.transform()));

        if (allAC.isEmpty())
            log.error("Empty list of Aesthetic care");

        return allAC;
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping("/{idAc}/" + AESTHETIC_CARE_PURCHASE)
    public ResponseEntity<SimpleResponseDTO> sessionPurchase(@PathVariable(name = "idAc") int idAc, @RequestParam(name = "idClient") int idClient,
                                                             @RequestParam(name = "customPrice", required = false, defaultValue = "-1") double customPrice,
                                                             @RequestParam(name = "idPaymentType", required = false, defaultValue = "1") int idPaymentType) {
        return SimpleResponseDTO.generateResponse(this.purchaseSessionService.purchaseSession(idClient, idAc, customPrice, idPaymentType));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_ALL_SESSION_PURCHASES)
    public List<SessionPurchaseDTO> getAllClientSessionPurchases(@RequestParam(name = "idClient") int idClient) {
        List<SessionPurchase> sessionPurchases = purchaseSessionService.getAllSessionPurchases(idClient);
        return sessionPurchases.stream().map(SessionPurchase::transform).toList();
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_ALL_SESSION_PURCHASES + "/{idSessionPurchase}")
    public SessionPurchaseDTO getClientSessionPurchase(@PathVariable(name = "idSessionPurchase") int idSessionPurchase) {
        SessionPurchase sessionPurchase = sessionPurchaseRepository.findByIdSessionPurchase(idSessionPurchase);
        if (sessionPurchase != null)
            return sessionPurchase.transform();
        else
            return null;
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(UNPAID_AESTHETIC_CARE_PURCHASES)
    public List<SessionPurchaseDTO> getAllUnpaidSessionPurchases(@RequestParam(name = "idClient") int idClient) {
        List<SessionPurchase> sessionPurchases = purchaseSessionService.getAllSessionPurchases(idClient);
        sessionPurchases = sessionPurchases.stream().filter(sP -> paymentService.billNotTotallyPaid(sP.getBill())).toList();
        return sessionPurchases.stream().map(SessionPurchase::transform).toList();
    }
}
