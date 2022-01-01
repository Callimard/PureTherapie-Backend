package puretherapie.crm.api.v1.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;

import javax.annotation.security.PermitAll;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Map;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.ClientController.API_V1_CLIENT_URL;

@Slf4j
@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientController {

    // Constants.

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/clients";

    // Variables.

    private final PersonOriginRepository personOriginRepository;
    private final ClientRepository clientRepository;

    // Constructors.

    public ClientController(PersonOriginRepository personOriginRepository, ClientRepository clientRepository) {
        this.personOriginRepository = personOriginRepository;
        this.clientRepository = clientRepository;
    }

    // Methods.

    @PostMapping
    @PermitAll
    @Transactional
    public ResponseEntity<Map<String, String>> clientRegistration(@RequestBody ClientInformation clientInformation) {
        Client c;
        try {
            c = createClient(clientInformation);
        } catch (ClientInformation.ClientInformationVerificationException e) {
            Map<String, String> error = e.getError();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        clientRepository.save(c);

        // TODO Verify client doubloon
        // TODO Add notification creation

        return ResponseEntity.ok(Collections.emptyMap());
    }

    private Client createClient(ClientInformation clientInformation) throws ClientInformation.ClientInformationVerificationException {
        clientInformation.verify();
        return clientInformation.buildClient(personOriginRepository);
    }
}
