package puretherapie.crm.api.v1.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.ClientRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.client.ClientController.API_V1_CLIENT_URL;

@Slf4j
@RestController
@RequestMapping(API_V1_CLIENT_URL)
public class ClientController {

    public static final String API_V1_CLIENT_URL = API_V1_URL + "/client";

    private final PersonOriginRepository personOriginRepository;

    private final ClientRepository clientRepository;

    public ClientController(PersonOriginRepository personOriginRepository, ClientRepository clientRepository) {
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

        log.error("PersonOrigin find = " + personOrigin);

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

        log.error("Client add in the DB => " + c);


        Client update = clientRepository.save(c);

        log.error("Client update = " + update);
    }

    @PutMapping
    public ResponseEntity<String> clientUpdate(@RequestParam(name = "idClient") long idClient, @RequestBody ClientInformation clientInformation) {

        PersonOrigin personOrigin = null;
        if (clientInformation.origin() != null)
            personOrigin = personOriginRepository.findByType(clientInformation.origin());

        log.error("PersonOrigin find = " + personOrigin);

        Client toUpdate = clientRepository.findByIdPerson(idClient);

        log.error("Client to update = " + toUpdate);

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

            log.error("New client = " + toUpdate);

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
