package puretherapie.crm.api.v1.person.client.controller.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientRegistrationFailDTO implements ClientRegistrationResponseDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String photo;
    private String comment;
    private String technicalComment;
    private String constraintViolation;
    private String dataIntegrity;
    private List<ClientDTO> doubloons;

    @Override
    public boolean isFailedResponse() {
        return true;
    }
}
