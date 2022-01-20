package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TakeAppointmentSuccessDTO implements TakeAppointmentResponseDTO {

    private String success;

    @Override
    public boolean isFailedResponse() {
        return false;
    }
}
