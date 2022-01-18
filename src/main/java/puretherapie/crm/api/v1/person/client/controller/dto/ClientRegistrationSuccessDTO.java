package puretherapie.crm.api.v1.person.client.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientRegistrationSuccessDTO implements ClientRegistrationResponseDTO {

    private int idClient;

    @Override
    public boolean isFailedResponse() {
        return false;
    }
}
