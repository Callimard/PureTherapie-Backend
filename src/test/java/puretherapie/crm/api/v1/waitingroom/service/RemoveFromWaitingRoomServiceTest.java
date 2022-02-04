package puretherapie.crm.api.v1.waitingroom.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.waitingroom.service.RemoveFromWaitingRoomService.CLIENT_ID_NOT_FOUND_ERROR;
import static puretherapie.crm.api.v1.waitingroom.service.RemoveFromWaitingRoomService.CLIENT_NOT_IN_WR_ERROR;

@SpringBootTest
@DisplayName("RemoveFromWaitingRoomService tests")
public class RemoveFromWaitingRoomServiceTest {

    @Autowired
    private RemoveFromWaitingRoomService rwrs;

    @Nested
    @DisplayName("Remove client test")
    class RemoveClient {

        @Test
        @DisplayName("Test with not found client fail")
        void testWithNotFoundClient() {
            prepareClientRepository();

            SimpleResponseDTO res = rwrs.removeClient(585);
            verifyFail(res);
            verifyFailType(res, CLIENT_ID_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with client not in waiting room fail")
        void testClientNotInWaitingRoom() {
            prepareClientRepository();
            prepareClientNotInWR();

            SimpleResponseDTO res = rwrs.removeClient(CLIENT_ID);
            verifyFail(res);
            verifyFailType(res, CLIENT_NOT_IN_WR_ERROR);
        }

        @Test
        @DisplayName("Test with all correct success")
        void testClientIsInWR() {
            prepareClientRepository();
            prepareClientIsInWR();

            SimpleResponseDTO res = rwrs.removeClient(CLIENT_ID);
            verifySuccess(res);
        }

    }

    private void verifySuccess(SimpleResponseDTO res) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isTrue();
    }

    private void verifyFail(SimpleResponseDTO res) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
    }

    void verifyFailType(SimpleResponseDTO res, String expectedKey) {
        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
        assertThat(res.message()).isEqualTo(expectedKey);
    }

    // Context.

    @MockBean
    private ClientRepository mockClientRepository;
    @Mock
    private Client mockClient;
    private static final int CLIENT_ID = 736;

    @MockBean
    private WaitingRoomRepository mockWRRepository;
    @Mock
    private WaitingRoom mockWR;

    private void prepareClientRepository() {
        given(mockClientRepository.findByIdPerson(CLIENT_ID)).willReturn(mockClient);
    }

    private void prepareClientIsInWR() {
        given(mockWRRepository.findByClient(mockClient)).willReturn(mockWR);
    }

    private void prepareClientNotInWR() {
        given(mockWRRepository.findByClient(mockClient)).willReturn(null);
    }

}
