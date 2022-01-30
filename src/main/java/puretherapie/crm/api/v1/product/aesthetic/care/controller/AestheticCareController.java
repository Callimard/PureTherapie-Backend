package puretherapie.crm.api.v1.product.aesthetic.care.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.SessionPurchaseDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.service.PurchaseSessionService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AestheticCareController.AESTHETIC_CARE_URL)
public class AestheticCareController {

    // Constants.

    public static final String AESTHETIC_CARE_URL = API_V1_URL + "/aesthetic_cares";

    public static final String AESTHETIC_CARE_PURCHASE = "/purchase";
    public static final String AESTHETIC_CARE_PURCHASE_URL = AESTHETIC_CARE_URL + AESTHETIC_CARE_PURCHASE;

    public static final String CLIENT_ALL_SESSION_PURCHASES = "/purchases";
    public static final String CLIENT_ALL_SESSION_PURCHASES_URL = AESTHETIC_CARE_URL + CLIENT_ALL_SESSION_PURCHASES;

    // Variables.

    private final AestheticCareRepository aestheticCareRepository;
    private final PurchaseSessionService purchaseSessionService;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @GetMapping
    public List<AestheticCareDTO> getAllAestheticCares() {
        List<AestheticCare> acList = this.aestheticCareRepository.findAll();
        List<AestheticCareDTO> allAC = new ArrayList<>();
        acList.forEach(ac -> allAC.add(ac.transform()));

        if (allAC.isEmpty())
            log.error("Empty list of Aesthetic care");

        return allAC;
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping("/{idAc}/" + AESTHETIC_CARE_PURCHASE)
    public ResponseEntity<SimpleResponseDTO> sessionPurchase(@PathVariable(name = "idAc") int idAc, @RequestParam(name = "idClient") int idClient,
                                                             @RequestParam(name = "customPrice", required = false, defaultValue = "-1") double customPrice,
                                                             @RequestParam(name = "idPaymentType", required = false, defaultValue = "1") int idPaymentType) {
        return SimpleResponseDTO.generateResponse(this.purchaseSessionService.purchaseSession(idClient, idAc, customPrice, idPaymentType));
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_ALL_SESSION_PURCHASES)
    public List<SessionPurchaseDTO> getAllClientSessionPurchases(@RequestParam(name = "idClient") int idClient) {
        List<SessionPurchase> sessionPurchases = purchaseSessionService.getAllSessionPurchases(idClient);
        return sessionPurchases.stream().map(SessionPurchase::transform).toList();
    }
}
