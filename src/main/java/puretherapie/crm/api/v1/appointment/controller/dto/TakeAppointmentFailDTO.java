package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TakeAppointmentFailDTO implements TakeAppointmentResponseDTO {

    private String error;

    @Override
    public boolean isFailedResponse() {
        return true;
    }
}
