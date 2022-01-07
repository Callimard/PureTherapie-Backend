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
        try {
            Client client = buildClient(clientInformation);
            verifyDoubloon(client, doubloonVerification);
            client = saveClient(client);
            notifyClientRegistration(client);
            return generateSuccessResponse(client);
        } catch (ClientRegistrationException e) {
            return Collections.singletonMap(ERROR_FIELD, e.getErrors());
        } catch (ClientDoubloonException e) {
            return Collections.singletonMap(CLIENT_DOUBLOON_FIELD, e.getDoubloonList());
        }
    }

    private void notifyClientRegistration(Client client) {
        boolean success =
                notificationCreationService.createNotification(NOTIFICATION_CLIENT_REGISTRATION_TITLE.formatted(client.simplyIdentifier()),
                                                               NOTIFICATION_CLIENT_REGISTRATION_TEXT.formatted(client.simplyIdentifier()),
                                                               BOSS_SECRETARY_LEVEL,
                                                               false);
        if (!success)
            log.error("Notification creation for client registration failed.");
    }

    private Map<String, Object> generateSuccessResponse(Client c) {
        Map<String, Object> res = new HashMap<>();
        res.put(SUCCESS_FIELD, "Client registration success");
        res.put(ID_CLIENT_FIELD, c.getIdPerson());

        return res;
    }

    private Client buildClient(ClientInformation clientInformation) throws ClientRegistrationException {
        try {
            return createClient(clientInformation);
        } catch (ClientInformation.ClientInformationVerificationException e) {
            log.debug("Exception during client information verification. Error = {}", e.getErrors());
            throw new ClientRegistrationException(e.getErrors());
        }
    }

    private void verifyDoubloon(Client c, boolean verify) throws ClientDoubloonException {
        if (verify) {
            List<Client> clients = clientRepository.findByFirstNameAndLastName(c.getFirstName(), c.getLastName());
            if (clients != null && !clients.isEmpty()) {
                List<ClientInformation> doubloons = new ArrayList<>();
                for (Client doubloon : clients) {
                    doubloons.add(doubloon.getClientInformation());
                }
                throw new ClientDoubloonException(doubloons);
            }
        }
    }

    private Client saveClient(Client c) throws ClientRegistrationException {
        try {
            Client saved = clientRepository.save(c);
            log.info("Client registration success. The new client {}", saved);
            return saved;
        } catch (DataIntegrityViolationException e) {
            Map<String, String> errors = new HashMap<>();
            treatDataIntegrityViolation(errors, e);
            throw new ClientRegistrationException(errors);
        }
    }

    private Client createClient(ClientInformation clientInformation) throws ClientInformation.ClientInformationVerificationException {
        clientInformation.verify();
        return clientInformation.buildClient(personOriginRepository);
    }

    private void treatDataIntegrityViolation(Map<String, String> errors, DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException constraintViolation)
            treatConstraintViolationCause(errors, constraintViolation);
        else
            treatUndefineViolationCause(errors, e);
    }

    private void treatConstraintViolationCause(Map<String, String> errors, ConstraintViolationException constraintViolation) {
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

    private void treatUndefineViolationCause(Map<String, String> errors, DataIntegrityViolationException e) {
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

    private static class ClientRegistrationException extends RuntimeException {

        private final Map<String, String> errors;

        public ClientRegistrationException(Map<String, String> errors) {
            super();
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }

    private static class ClientDoubloonException extends RuntimeException {

        private final transient List<ClientInformation> doubloonList;

        public ClientDoubloonException(List<ClientInformation> doubloonList) {
            super();
            this.doubloonList = doubloonList;
        }

        public List<ClientInformation> getDoubloonList() {
            return doubloonList;
        }
    }
}
