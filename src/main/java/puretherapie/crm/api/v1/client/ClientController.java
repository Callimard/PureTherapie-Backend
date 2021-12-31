package puretherapie.crm.api.v1.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;
import puretherapie.crm.tool.PhoneTool;

import javax.annotation.security.PermitAll;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.ClientController.API_V1_CLIENT_URL;
import static puretherapie.crm.data.person.Person.NO_PHONE;
import static puretherapie.crm.data.person.PersonOrigin.NONE_TYPE;
import static puretherapie.crm.tool.MailTool.isValidMail;
import static puretherapie.crm.tool.PhoneTool.formatPhone;

@Slf4j
@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientController {

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/clients";

    private final PersonOriginRepository personOriginRepository;

    private final ClientRepository clientRepository;

    public ClientController(PersonOriginRepository personOriginRepository, ClientRepository clientRepository) {
        this.personOriginRepository = personOriginRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    @PermitAll
    @Transactional
    public void clientRegistration(@RequestBody ClientInformation clientInformation) {
        PersonOrigin personOrigin = getPersonOrigin(clientInformation);

        Client c;
        try {
            c = createClient(clientInformation, personOrigin);
            log.error("Client add in the DB => " + c);
            Client update = clientRepository.save(c);
            log.error("Client update = " + update);
        } catch (ClientPhoneException e) {
            // TODO Manage ResponseEntity.
            e.printStackTrace();
        } catch (ClientMailException e) {
            // TODO Manage ResponseEntity.
            e.printStackTrace();
        }
    }

    private PersonOrigin getPersonOrigin(ClientInformation clientInformation) {
        PersonOrigin personOrigin;
        return (personOrigin = personOriginRepository.findByIdPersonOrigin(clientInformation.idOrigin())) == null ? noneOrigin() : personOrigin;
    }

    private PersonOrigin noneOrigin() {
        return personOriginRepository.findByType(NONE_TYPE);
    }

    private Client createClient(ClientInformation clientInformation, PersonOrigin personOrigin) throws ClientPhoneException, ClientMailException {

        String phone = extractPhoneNumber(clientInformation);
        String mail = extractMail(clientInformation);

        return Client.builder().photo(clientInformation.photo())
                .comment(clientInformation.comment())
                .technicalComment(clientInformation.technicalComment())
                .firstName(clientInformation.firstName())
                .lastName(clientInformation.lastName())
                .mail(mail)
                .gender(clientInformation.gender())
                .birthday(clientInformation.birthday())
                .phone(phone)
                .creationDate(OffsetDateTime.now())
                .personOrigin(personOrigin).build();
    }

    private String extractPhoneNumber(ClientInformation clientInformation) throws ClientPhoneException {
        return clientInformation.phone().isBlank() ? NO_PHONE : verifyPhoneNumber(clientInformation.phone());
    }

    private String verifyPhoneNumber(String phone) throws ClientPhoneException {
        try {
            return formatPhone(phone);
        } catch (PhoneTool.FailToFormatPhoneNumber e) {
            throw new ClientPhoneException("ClientPhone wrong format");
        } catch (PhoneTool.UnSupportedPhoneNumberException e) {
            throw new ClientPhoneException("ClientPhone country unsupported");
        } catch (PhoneTool.NotPhoneNumberException e) {
            throw new ClientPhoneException("ClientPhone not a phone number");
        }
    }

    private String extractMail(ClientInformation clientInformation) throws ClientMailException {
        return verifyMail(clientInformation.mail());
    }

    private String verifyMail(String mail) throws ClientMailException {
        if (mail == null || mail.isBlank() || !isValidMail(mail))
            throw new ClientMailException("ClientMail wrong format");

        return mail;
    }

    // Exceptions.

    private static class ClientPhoneException extends Exception {
        public ClientPhoneException(String message) {
            super(message);
        }
    }

    private static class ClientMailException extends Exception {
        public ClientMailException(String message) {
            super(message);
        }
    }
}
