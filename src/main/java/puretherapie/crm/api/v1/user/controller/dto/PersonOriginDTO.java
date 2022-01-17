package puretherapie.crm.api.v1.user.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PersonOriginDTO {

    private int idPersonOrigin;
    private String type;

}
