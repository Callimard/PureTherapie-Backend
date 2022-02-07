package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionalCloseDTO {

    private int idExceptionalClose;
    private String day;

}
