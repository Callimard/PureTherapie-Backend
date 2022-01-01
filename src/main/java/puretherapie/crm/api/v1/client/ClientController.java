package puretherapie.crm.api.v1.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;

import javax.annotation.security.PermitAll;
import javax.transaction.Transactional;

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
    public void clientRegistration(@RequestBody ClientInformation clientInformation) {
        Client c;
        try {
            c = createClient(clientInformation);
            log.error("Client add in the DB => " + c);
            Client update = clientRepository.save(c);
            log.error("Client update = " + update);
        } catch (ClientInformation.ClientPhotoException e) {
            // TODO Manage ResponseEntity
            e.printStackTrace();
        } catch (ClientInformation.ClientLastNameException e) {
            e.printStackTrace();
        } catch (ClientInformation.ClientTechnicalCommentException e) {
            e.printStackTrace();
        } catch (ClientInformation.ClientMailException e) {
            e.printStackTrace();
        } catch (ClientInformation.ClientPhoneException e) {
            e.printStackTrace();
        } catch (ClientInformation.ClientCommentException e) {
            e.printStackTrace();
        } catch (ClientInformation.ClientFirstNameException e) {
            e.printStackTrace();
        }
    }

    private Client createClient(ClientInformation clientInformation)
            throws ClientInformation.ClientPhotoException, ClientInformation.ClientLastNameException,
                   ClientInformation.ClientTechnicalCommentException, ClientInformation.ClientMailException, ClientInformation.ClientPhoneException,
                   ClientInformation.ClientCommentException, ClientInformation.ClientFirstNameException {

        clientInformation.verify();
        return clientInformation.buildClient(personOriginRepository);
    }
}
