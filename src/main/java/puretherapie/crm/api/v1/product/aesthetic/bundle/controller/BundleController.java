package puretherapie.crm.api.v1.product.aesthetic.bundle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundleDTO;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundlePurchaseDTO;
import puretherapie.crm.api.v1.product.aesthetic.bundle.service.BundlePurchaseService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.product.aesthetic.bundle.controller.BundleController.BUNDLES_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(BUNDLES_URL)
public class BundleController {

    // Constants.

    public static final String BUNDLES_URL = API_V1_URL + "/bundles";

    public static final String CLIENT_BUNDLE_PURCHASE = "/{idBundle}/purchase";
    public static final String CLIENT_BUNDLE_PURCHASE_URL = BUNDLES_URL + CLIENT_BUNDLE_PURCHASE;

    public static final String CLIENT_ALL_BUNDLE_PURCHASES = "/purchases";
    public static final String CLIENT_ALL_BUNDLE_PURCHASES_URL = BUNDLES_URL + CLIENT_ALL_BUNDLE_PURCHASES;

    // Variables.

    private final BundleRepository bundleRepository;
    private final BundlePurchaseService bundlePurchaseService;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public List<BundleDTO> getAllBundles() {
        List<Bundle> bundleList = this.bundleRepository.findAll();
        List<BundleDTO> allBundles = new ArrayList<>();
        bundleList.forEach(bundle -> allBundles.add(bundle.transform()));

        if (allBundles.isEmpty())
            log.error("Empty list for Bundles");

        return allBundles;
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(CLIENT_BUNDLE_PURCHASE)
    public ResponseEntity<SimpleResponseDTO> bundlePurchase(@PathVariable(name = "idBundle") int idBundle,
                                                            @RequestParam(name = "idClient") int idClient,
                                                            @RequestParam(name = "customPrice", required = false, defaultValue = "-1") double customPrice,
                                                            @RequestParam(name = "idPaymentType", required = false, defaultValue = "1") int idPaymentType) {
        return SimpleResponseDTO.generateResponse(this.bundlePurchaseService.purchaseBundle(idBundle, idClient, Collections.emptySet(), customPrice
                , idPaymentType));
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_ALL_BUNDLE_PURCHASES)
    public List<BundlePurchaseDTO> getAllClientBundlePurchases(@RequestParam(name = "idClient") int idClient) {
        List<BundlePurchase> bundlePurchases = bundlePurchaseService.getAllClientBundlePurchases(idClient);
        return bundlePurchases.stream().map(BundlePurchase::transform).toList();
    }

}
