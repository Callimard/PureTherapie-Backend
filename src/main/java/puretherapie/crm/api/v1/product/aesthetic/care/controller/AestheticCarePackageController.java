package puretherapie.crm.api.v1.product.aesthetic.care.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCarePackageDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCarePackageRepository;

import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.product.aesthetic.care.controller.AestheticCarePackageController.AESTHETIC_CARE_PACKAGES_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AESTHETIC_CARE_PACKAGES_URL)
public class AestheticCarePackageController {

    // Constants.

    public static final String AESTHETIC_CARE_PACKAGES_URL = API_V1_URL + "/aesthetic_care_packages";

    // Variables.

    private final AestheticCarePackageRepository aestheticCarePackageRepository;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public List<AestheticCarePackageDTO> getAllAestheticCarePackages() {
        List<AestheticCarePackage> acpList = this.aestheticCarePackageRepository.findAll();
        List<AestheticCarePackageDTO> allACP = new ArrayList<>();
        acpList.forEach(acp -> allACP.add(acp.transform()));

        if (allACP.isEmpty())
            log.error("Empty list of Aesthetic care package");

        return allACP;
    }

}
