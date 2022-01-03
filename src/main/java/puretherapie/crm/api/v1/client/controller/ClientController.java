package puretherapie.crm.api.v1.client.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.client.ClientInformation;
import puretherapie.crm.api.v1.client.service.ClientRegistrationService;

import java.util.Map;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.controller.ClientController.API_V1_CLIENT_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientController {

    // Constants.

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/clients";

    public static final String PARAM_DOUBLOON_VERIFICATION = "doubloonVerification";

    public static final String CLIENT_DOUBLOON_FIELD = "client_doubloon";

    // Variables.

    private final ClientRegistrationService clientRegistrationService;

    // Methods.

    @PostMapping
    public ResponseEntity<Map<String, Object>> clientRegistration(@RequestParam(value = PARAM_DOUBLOON_VERIFICATION, required = false,
            defaultValue = "true") boolean doubloonVerification, @RequestBody ClientInformation clientInformation) {
        return clientRegistrationService.noTransactionalClientRegistration(clientInformation, doubloonVerification);
    }
}
