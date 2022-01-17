package puretherapie.crm.api.v1.client.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.client.controller.dto.ClientRegistrationResponseDTO;
import puretherapie.crm.api.v1.client.service.ClientRegistrationService;
import puretherapie.crm.api.v1.user.controller.dto.PersonOriginDTO;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.controller.ClientController.CLIENT_URL;
import static puretherapie.crm.api.v1.client.service.ClientRegistrationService.CLIENT_DOUBLOON_FIELD;
import static puretherapie.crm.api.v1.client.service.ClientRegistrationService.CLIENT_REGISTRATION_FAIL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(CLIENT_URL)
public class ClientController {

    // Constants.

    public static final String CLIENT_URL = API_V1_URL + "/clients";
    public static final String PERSON_ORIGINS = "/personOrigins";
    public static final String PERSON_ORIGINS_URL = CLIENT_URL + PERSON_ORIGINS;

    public static final String PARAM_DOUBLOON_VERIFICATION = "doubloonVerification";

    // Variables.

    private final ClientRegistrationService clientRegistrationService;
    private final PersonOriginRepository personOriginRepository;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "false")
    @PostMapping
    public ResponseEntity<ClientRegistrationResponseDTO> clientRegistration(@RequestParam(value = PARAM_DOUBLOON_VERIFICATION, required = false,
            defaultValue = "true") boolean doubloonVerification, @RequestBody ClientDTO clientDTO,
                                                                  Authentication authentication) {

        doubloonVerification = verifyPermissionForDoubloonVerification(doubloonVerification, authentication);

        ClientRegistrationResponseDTO responseDTO = clientRegistrationService.clientRegistration(clientDTO, doubloonVerification);
        if (responseDTO.isFailedResponse())
            return ResponseEntity.badRequest().body(responseDTO);
        else
            return ResponseEntity.ok(responseDTO);
    }

    private boolean verifyPermissionForDoubloonVerification(boolean doubloonVerification, Authentication authentication) {
        if ((authentication == null || !authentication.isAuthenticated()) && !doubloonVerification) {
            log.info("Cannot not make the doubloon verification if no authenticated");
            doubloonVerification = true;
        }
        return doubloonVerification;
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "false")
    @GetMapping(PERSON_ORIGINS)
    public ResponseEntity<List<PersonOriginDTO>> getAllPersonOrigin() {
        List<PersonOrigin> personOrigins = personOriginRepository.findAll();
        List<PersonOriginDTO> allPersonOrigins = new ArrayList<>();

        for (PersonOrigin personOrigin : personOrigins) {
            allPersonOrigins.add(personOrigin.buildDTO());
        }

        return ResponseEntity.ok(allPersonOrigins);
    }
}
