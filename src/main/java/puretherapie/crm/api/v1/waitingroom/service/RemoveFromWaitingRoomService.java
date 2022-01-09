package puretherapie.crm.api.v1.waitingroom.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class RemoveFromWaitingRoomService extends SimpleService {

    // Constants.

    public static final String CLIENT_REMOVE_FROM_WR_SUCCESS = "client_remove_wr_success";
    public static final String CLIENT_REMOVE_FROM_WR_FAIL = "client_remove_wr_fail";

    public static final String CLIENT_ID_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String CLIENT_NOT_IN_WR_ERROR = "client_not_in_wr_error";

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
    public Map<String, Object> removeClient(int idClient) {
        try {
            WaitingRoom waitingRoom = verifyAndGetWR(idClient);
            removeWR(waitingRoom);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to remove client from waiting room, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private WaitingRoom verifyAndGetWR(int idClient) {
        Client client = verifyClient(idClient);
        WaitingRoom wr = waitingRoomRepository.findByClient(client);
        if (wr == null)
            throw new RemoveFromWaitingRoomException("Client not in waiting room",
                                                     generateError(CLIENT_NOT_IN_WR_ERROR, "Client not in waiting room"));

        return wr;
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new RemoveFromWaitingRoomException("Not find client for idClient %s".formatted(idClient),
                                                     generateError(CLIENT_ID_NOT_FOUND_ERROR, "Client id not found"));
        return client;
    }

    private void removeWR(WaitingRoom waitingRoom) {
        waitingRoomRepository.delete(waitingRoom);
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return CLIENT_REMOVE_FROM_WR_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return CLIENT_REMOVE_FROM_WR_FAIL;
    }

    // Exceptions.

    private static class RemoveFromWaitingRoomException extends SimpleService.ServiceException {
        public RemoveFromWaitingRoomException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
