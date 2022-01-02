package puretherapie.crm.api.v1.client;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;

import java.util.*;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.ClientController.API_V1_CLIENT_URL;
import static puretherapie.crm.data.person.Person.EMAIL_FIELD;
import static puretherapie.crm.data.person.Person.PHONE_FIELD;

@Slf4j
@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientController {

    // Constants.

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/clients";

    public static final String SUCCESS_FIELD = "success";
    public static final String ERROR_FIELD = "error";
    public static final String CLIENT_DOUBLOON_FIELD = "client_doubloon";

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
    public ResponseEntity<Map<String, Object>> clientRegistration(@RequestParam("doubloonVerification") boolean doubloonVerification,
                                                                  @RequestBody ClientInformation clientInformation) {
        log.debug("In client registration with ClientInformation = {}", clientInformation);

        Map<String, Object> errors = new HashMap<>();

        // Build client form client information
        Client c = tryBuildClient(clientInformation, errors);
        if (c == null || !errors.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap(ERROR_FIELD, errors));

        // Verify doubloon if needed
        List<ClientInformation> doubloon = null;
        if (doubloonVerification) {
            doubloon = verifyDoubloon(c);
        }

        if (doubloon == null || doubloon.isEmpty()) {
            // Save the client
            trySaveClient(errors, c);
            if (!errors.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap(ERROR_FIELD, errors));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap(CLIENT_DOUBLOON_FIELD, doubloon));
        }

        // TODO Add notification creation

        return ResponseEntity.ok(Collections.singletonMap(SUCCESS_FIELD, "Client registration success"));
    }

    private Client tryBuildClient(ClientInformation clientInformation, Map<String, Object> errors) {
        try {
            return createClient(clientInformation);
        } catch (ClientInformation.ClientInformationVerificationException e) {
            log.debug("Exception during client information verification. Error = {}", errors);
            errors.putAll(e.getErrors());
            return null;
        }
    }

    private List<ClientInformation> verifyDoubloon(Client c) {
        List<Client> clients = clientRepository.findByFirstNameAndLastName(c.getFirstName(), c.getLastName());
        if (clients != null && !clients.isEmpty()) {
            List<ClientInformation> doubloons = new ArrayList<>();
            for (Client doubloon : clients) {
                doubloons.add(doubloon.getClientInformation());
            }
            return doubloons;
        } else {
            return Collections.emptyList();
        }
    }

    private void trySaveClient(Map<String, Object> errors, Client c) {
        try {
            Client saved = clientRepository.save(c);
            log.info("Client registration of the client {}", saved);
        } catch (DataIntegrityViolationException e) {
            treatDataIntegrityViolation(errors, e);
        }
    }

    private Client createClient(ClientInformation clientInformation) throws ClientInformation.ClientInformationVerificationException {
        clientInformation.verify();
        return clientInformation.buildClient(personOriginRepository);
    }

    private void treatDataIntegrityViolation(Map<String, Object> errors, DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException constraintViolation)
            treatConstraintViolationCause(errors, constraintViolation);
        else
            treatUndefineViolationCause(errors, e);
    }

    private void treatConstraintViolationCause(Map<String, Object> errors, ConstraintViolationException constraintViolation) {
        log.debug("Detect constraint violation.");
        String fieldViolated = extractNotUniqueField(constraintViolation);
        if (fieldViolated != null) {
            log.debug("Unique constraint violation of the field {}", fieldViolated);
            errors.put(fieldViolated, "Already client with this " + fieldViolated);
        } else {
            log.error("ConstraintViolation with not found violated field, constraint name = {}", constraintViolation.getConstraintName());
            errors.put(ERROR_FIELD, "ConstraintViolation");
        }
    }

    private void treatUndefineViolationCause(Map<String, Object> errors, DataIntegrityViolationException e) {
        log.debug("Detect DataIntegrityViolationException", e);
        errors.put(ERROR_FIELD, e.getMessage());
    }

    private String extractNotUniqueField(ConstraintViolationException constraintViolation) {
        if (constraintViolation.getConstraintName().contains(PHONE_FIELD)) {
            return PHONE_FIELD;
        } else if (constraintViolation.getConstraintName().contains(EMAIL_FIELD)) {
            return EMAIL_FIELD;
        } else
            return null;
    }
}
