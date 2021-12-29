package puretherapie.crm.api.v1.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.person.PersonOrigin;
import puretherapie.crm.person.PersonOriginRepository;
import puretherapie.crm.person.client.data.Client;
import puretherapie.crm.person.client.data.ClientRepository;
import puretherapie.crm.person.client.service.request.ClientInformation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.ClientManager.API_V1_CLIENT_URL;

@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientManager {

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/client";

    private final PersonOriginRepository personOriginRepository;

    private final ClientRepository clientRepository;

    public ClientManager(PersonOriginRepository personOriginRepository, ClientRepository clientRepository) {
        this.personOriginRepository = personOriginRepository;
        this.clientRepository = clientRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/registration")
    public void clientRegistration(@RequestBody ClientInformation clientInformation) {
        PersonOrigin personOrigin = null;
        if (clientInformation.origin() != null)
            personOrigin = personOriginRepository.findByType(clientInformation.origin());

        System.out.println("PersonOrigin find = " + personOrigin);

        Client c = Client.builder().photo(clientInformation.photo())
                .comment(clientInformation.comment())
                .technicalComment(clientInformation.technicalComment())
                .firstName(clientInformation.firstName())
                .lastName(clientInformation.lastName())
                .mail(clientInformation.mail())
                .gender(clientInformation.gender())
                .birthday(clientInformation.birthday())
                .phone(clientInformation.phone())
                .creationDate(OffsetDateTime.now())
                .personOrigin(personOrigin).build();

        System.out.println("Client add in the DB => " + c);


        Client update = clientRepository.save(c);

        System.out.println("Client update = " + update);
    }

    @PutMapping
    public ResponseEntity<String> clientUpdate(@RequestParam(name = "idClient") long idClient, @RequestBody ClientInformation clientInformation) {

        PersonOrigin personOrigin = null;
        if (clientInformation.origin() != null)
            personOrigin = personOriginRepository.findByType(clientInformation.origin());

        System.out.println("PersonOrigin find = " + personOrigin);

        Client toUpdate = clientRepository.findByIdPerson(idClient);

        System.out.println("Client to update = " + toUpdate);

        if (toUpdate != null) {
            toUpdate.setPhoto(clientInformation.photo());
            toUpdate.setComment(clientInformation.comment());
            toUpdate.setTechnicalComment(clientInformation.technicalComment());
            toUpdate.setFirstName(clientInformation.firstName());
            toUpdate.setLastName(clientInformation.lastName());
            toUpdate.setMail(clientInformation.mail());
            toUpdate.setGender(clientInformation.gender());
            toUpdate.setBirthday(clientInformation.birthday());
            toUpdate.setPhone((clientInformation.phone()));
            toUpdate.setPersonOrigin(personOrigin);

            System.out.println("New client = " + toUpdate);

            clientRepository.save(toUpdate);

            String success = """
                        {
                            "success": "Client with id %d correctly update"
                        }
                    """.formatted(idClient);

            return ResponseEntity.ok().header("Content-Type", "application/json").body(success);
        } else {
            String error = """
                    {
                        "error": "Client with the id %d does not exist"
                    }
                    """.formatted(idClient);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json")
                    .body(error);
        }
    }

    @GetMapping
    public String getClient() {
        return "Call getClient";
    }

}
