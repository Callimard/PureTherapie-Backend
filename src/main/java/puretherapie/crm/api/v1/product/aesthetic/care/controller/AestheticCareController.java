package puretherapie.crm.api.v1.product.aesthetic.care.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
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

    // Variables.

    private final AestheticCareRepository aestheticCareRepository;

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

}
