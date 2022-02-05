package puretherapie.crm.api.v1.waitingroom.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

@Slf4j
@AllArgsConstructor
@Service
public class RemoveFromWaitingRoomService {

    // Constants.
    public static final String CLIENT_ID_NOT_FOUND_ERROR = "client_not_found_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final WaitingRoomRepository waitingRoomRepository;

    // Methods.

    /**
     * Verify if client is found and client is in waiting room.
     *
     * @param idClient the id client to remove
     *
     * @return the res of the try to remove client from waiting room
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO removeClient(int idClient) {
        try {
            WaitingRoom waitingRoom = getWR(idClient);
            if (waitingRoom != null)
                removeWR(waitingRoom);
            else
                log.debug("Remove client from WR which is not in the WR.");
            return SimpleResponseDTO.generateSuccess("Success to remove from WR");
        } catch (Exception e) {
            log.debug("Fail to remove client from waiting room, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private WaitingRoom getWR(int idClient) {
        Client client = verifyClient(idClient);
        return waitingRoomRepository.findByClient(client);
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new RemoveFromWaitingRoomException(CLIENT_ID_NOT_FOUND_ERROR);
        return client;
    }

    private void removeWR(WaitingRoom waitingRoom) {
        waitingRoomRepository.delete(waitingRoom);
    }

    // SimpleService methods.

    // Exceptions.

    private static class RemoveFromWaitingRoomException extends RuntimeException {
        public RemoveFromWaitingRoomException(String message) {
            super(message);
        }
    }
}
