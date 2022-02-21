package puretherapie.crm.api.v1.person.client.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientRegistrationFailDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientRegistrationResponseDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientRegistrationSuccessDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.data.person.Person.*;
import static puretherapie.crm.data.person.client.Client.*;

@Slf4j
@AllArgsConstructor
@Service
public class ClientRegistrationService {

    // Constants.

    private static final String NOTIFICATION_CLIENT_REGISTRATION_TITLE = "Enregistrement client";
    private static final String NOTIFICATION_CLIENT_REGISTRATION_TEXT = "Le/la client(e) %s vient d'être enregistré(e)";

    // Variables.

    private final PersonOriginRepository personOriginRepository;
    private final ClientRepository clientRepository;
    private final NotificationCreationService notificationCreationService;

    // Methods.

    @Transactional(propagation = Propagation.NEVER)
    public ClientRegistrationResponseDTO clientRegistration(ClientDTO clientDTO, boolean doubloonVerification) {
        try {
            Client client = buildClient(clientDTO);
            verifyDoubloon(client, doubloonVerification);
            client = saveClient(client);
            notifyClientRegistration(client);
            return generateSuccessResponse(client);
        } catch (ClientFieldException e) {
            ClientRegistrationFailDTO fail = e.buildFailResponse();
            log.error("Client field error, Err = {}", fail);
            return fail;
        } catch (ClientDoubloonException e) {
            log.error("Client double error, Err = {}", e.getMessage());
            return ClientRegistrationFailDTO.builder().doubloons(e.getDoubloonList()).build();
        } catch (DataIntegrityViolationException e) {
            log.error("Client data integrity error, Err = {}", e.getMessage());
            return generateDataIntegrityFailResponse(e);
        }
    }

    private void notifyClientRegistration(Client client) {
        boolean success =
                notificationCreationService.createNotification(NOTIFICATION_CLIENT_REGISTRATION_TITLE,
                                                               NOTIFICATION_CLIENT_REGISTRATION_TEXT.formatted(client.simplyIdentifier()),
                                                               BOSS_SECRETARY_LEVEL,
                                                               false);
        if (!success)
            log.error("Notification creation for client registration failed.");
    }

    private ClientRegistrationSuccessDTO generateSuccessResponse(Client c) {
        return ClientRegistrationSuccessDTO.builder().idClient(c.getIdPerson()).build();
    }

    private Client buildClient(ClientDTO clientDTO) throws ClientFieldException {
        try {
            return createClient(clientDTO);
        } catch (ClientDTO.ClientInformationVerificationException e) {
            log.info("Exception during client information verification. Error = {}", e.getErrors());
            throw new ClientFieldException(e.getErrors());
        }
    }

    private void verifyDoubloon(Client c, boolean verify) throws ClientDoubloonException {
        if (verify) {
            List<Client> clients = clientRepository.findByFirstNameAndLastName(c.getFirstName(), c.getLastName());
            if (clients != null && !clients.isEmpty()) {
                List<ClientDTO> doubloons = new ArrayList<>();
                for (Client doubloon : clients) {
                    doubloons.add(doubloon.transform());
                }
                throw new ClientDoubloonException(doubloons);
            }
        }
    }

    private Client saveClient(Client c) throws ClientFieldException {
        Client saved = clientRepository.save(c);
        log.info("Client registration success. The new client {}", saved);
        return saved;
    }

    private Client createClient(ClientDTO clientDTO) throws ClientDTO.ClientInformationVerificationException {
        clientDTO.verify();
        return clientDTO.buildClient(personOriginRepository);
    }

    private ClientRegistrationFailDTO generateDataIntegrityFailResponse(DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException constraintViolation) {
            log.info("Client registration with constraint violation => constraint {}", constraintViolation.getConstraintName());
            return ClientRegistrationFailDTO.builder().constraintViolation(constraintViolation.getConstraintName()).build();
        } else {
            log.info("Client registration with data integrity error => {}", e.getMessage());
            return ClientRegistrationFailDTO.builder().dataIntegrity(e.getMessage()).build();
        }
    }

    // SimpleService methods.

    private static class ClientFieldException extends RuntimeException {

        private final Map<String, String> errors;

        public ClientFieldException(Map<String, String> errors) {
            super();
            this.errors = errors;
        }

        public ClientRegistrationFailDTO buildFailResponse() {
            return ClientRegistrationFailDTO.builder()
                    .firstName(errors.get(FIRST_NAME_FIELD))
                    .lastName(errors.get(LAST_NAME_FIELD))
                    .email(errors.get(EMAIL_FIELD))
                    .phone(errors.get(PHONE_FIELD))
                    .photo(errors.get(PHOTO_FIELD))
                    .comment(errors.get(COMMENT_FIELD))
                    .technicalComment(errors.get(TECHNICAL_COMMENT_FIELD))
                    .build();
        }
    }

    private static class ClientDoubloonException extends RuntimeException {

        private final transient List<ClientDTO> doubloonList;

        public ClientDoubloonException(List<ClientDTO> doubloonList) {
            super();
            this.doubloonList = doubloonList;
        }

        public List<ClientDTO> getDoubloonList() {
            return doubloonList;
        }
    }
}
