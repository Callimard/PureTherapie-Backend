package puretherapie.crm.api.v1.client.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import puretherapie.crm.api.v1.client.ClientInformation;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

import java.util.*;

import static puretherapie.crm.api.v1.client.controller.ClientController.CLIENT_DOUBLOON_FIELD;
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.data.person.Person.*;
import static puretherapie.crm.tool.ControllerTool.ERROR_FIELD;
import static puretherapie.crm.tool.ControllerTool.SUCCESS_FIELD;

@Slf4j
@AllArgsConstructor
@Service
public class ClientRegistrationService {

    // Constants.

    private static final String NOTIFICATION_CLIENT_REGISTRATION_TITLE = "Registration of the client %s";
    private static final String NOTIFICATION_CLIENT_REGISTRATION_TEXT = "The client %s has been register";

    public static final String ID_CLIENT_FIELD = "idClient";

    // Variables.

    private final PersonOriginRepository personOriginRepository;
    private final ClientRepository clientRepository;
    private final NotificationCreationService notificationCreationService;

    // Methods.

    @Transactional(propagation = Propagation.NEVER)
    public Map<String, Object> clientRegistration(ClientInformation clientInformation, boolean doubloonVerification) {
        log.debug("Client registration for ClientInformation = {}", clientInformation);

        Map<String, Object> errors = new HashMap<>();

        // Build client form client information
        Client c = tryBuildClient(clientInformation, errors);
        if (c == null || !errors.isEmpty())
            return Collections.singletonMap(ERROR_FIELD, errors);

        // Verify doubloon if needed
        List<ClientInformation> doubloon = null;
        if (doubloonVerification) {
            doubloon = verifyDoubloon(c);
        }

        if (doubloon == null || doubloon.isEmpty()) {
            // Save the client
            c = trySaveClient(errors, c);
            if (c == null || !errors.isEmpty())
                return Collections.singletonMap(ERROR_FIELD, errors);
        } else {
            log.info("Doubloon(s) find for the client {}", clientInformation);
            return Collections.singletonMap(CLIENT_DOUBLOON_FIELD, doubloon);
        }

        boolean success = createClientRegistrationNotification(c);

        if (!success)
            log.error("Notification creation for client registration failed.");

        return generateSuccessResponse(c);
    }

    private boolean createClientRegistrationNotification(Client client) {
        return notificationCreationService.createNotification(NOTIFICATION_CLIENT_REGISTRATION_TITLE.formatted(client.simplyIdentifier()),
                                                              NOTIFICATION_CLIENT_REGISTRATION_TEXT.formatted(client.simplyIdentifier()),
                                                              BOSS_SECRETARY_LEVEL,
                                                              false);
    }

    private Map<String, Object> generateSuccessResponse(Client c) {
        Map<String, Object> res = new HashMap<>();
        res.put(SUCCESS_FIELD, "Client registration success");
        res.put(ID_CLIENT_FIELD, c.getIdPerson());

        return res;
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

    private Client trySaveClient(Map<String, Object> errors, Client c) {
        try {
            Client saved = clientRepository.save(c);
            log.info("Client registration success. The new client {}", saved);
            return saved;
        } catch (DataIntegrityViolationException e) {
            treatDataIntegrityViolation(errors, e);
            return null;
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
        if (constraintViolation.getConstraintName().equals(UNIQUE_PHONE_CONSTRAINTS)) {
            return PHONE_FIELD;
        } else if (constraintViolation.getConstraintName().equals(UNIQUE_EMAIL_CONSTRAINTS)) {
            return EMAIL_FIELD;
        } else
            return null;
    }
}
