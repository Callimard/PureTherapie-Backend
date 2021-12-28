package puretherapie.crm.person.client.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.person.PersonOrigin;
import puretherapie.crm.person.PersonOriginRepository;
import puretherapie.crm.person.client.data.Client;
import puretherapie.crm.person.client.data.ClientRegistrationRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;

@RestController("ClientApi")
@RequestMapping("/api/v1/client")
public class ClientRestApi {

    private PersonOriginRepository personOriginRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/registration")
    public void clientRegistration(@RequestBody ClientRegistrationRequest request) {
        PersonOrigin personOrigin = null;
        if (request.origin() != null)
            personOrigin = personOriginRepository.findByType(request.origin());

        Client c = Client.builder().photo(request.photo())
                .comment(request.comment())
                .technicalComment(request.technicalComment())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .mail(request.mail())
                .gender(request.gender())
                .birthday(request.birthday())
                .phone(request.phone())
                .creationDate(OffsetDateTime.now())
                .personOrigin(personOrigin).build();

        System.out.println("Client add in the DB => " + c);

        try {
            entityManager.persist(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
