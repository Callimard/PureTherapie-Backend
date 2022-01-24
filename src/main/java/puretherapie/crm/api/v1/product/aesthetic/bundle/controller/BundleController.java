package puretherapie.crm.api.v1.product.aesthetic.bundle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundleDTO;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundleRepository;

import java.util.ArrayList;
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

    // Variables.

    private final BundleRepository bundleRepository;

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

}
