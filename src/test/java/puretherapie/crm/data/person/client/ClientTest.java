package puretherapie.crm.data.person.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import puretherapie.crm.data.appointment.Appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client tests")
public class ClientTest {

    private Client client = Client.builder().build();
    private static final int CLIENT_ID = 963;
    private static final int OTHER_CLIENT_ID = CLIENT_ID - 21;

    @Mock
    private Appointment mockAppointment;

    @Nested
    @DisplayName("IsAssociateTo test")
    class IsAssociateTo {

        @Test
        @DisplayName("Test isAssociateTo throws NullPointerException if appointment is null")
        void testWithNullAppointment() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> client.isAssociateTo(null));
        }

        @Test
        @DisplayName("Test isAssociateTo returns false if appointment does not have the same client id than the client")
        void testWithDifferentClientId() {
            client.setIdPerson(CLIENT_ID);
            given(mockAppointment.getClient()).willReturn(Client.builder().idPerson(OTHER_CLIENT_ID).build());

            assertThat(client.isAssociateTo(mockAppointment)).isFalse();
        }

        @Test
        @DisplayName("Test isAssociateTo returns false if appointment does not have the same client id than the client")
        void testWithSameClientId() {
            client.setIdPerson(CLIENT_ID);
            given(mockAppointment.getClient()).willReturn(Client.builder().idPerson(CLIENT_ID).build());

            assertThat(client.isAssociateTo(mockAppointment)).isTrue();
        }

    }

}
