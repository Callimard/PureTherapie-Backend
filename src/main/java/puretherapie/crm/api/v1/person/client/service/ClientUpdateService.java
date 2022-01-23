package puretherapie.crm.api.v1.person.client.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

@Slf4j
@AllArgsConstructor
@Service
public class ClientUpdateService {

    // Variables.

    private final ClientRepository clientRepository;
    private final PersonOriginRepository personOriginRepository;

    public ClientDTO updateClient(ClientDTO clientDTO) {
        try {
            return clientRepository.save(clientDTO.buildClient(personOriginRepository)).transform();
        } catch (Exception e) {
            log.error("Fail to update client, error msg = {}", e.getMessage());
            return null;
        }
    }
}
