package puretherapie.crm.api.v1.person.client.controller.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientRemainingStockPayDTO {

    private int remainingStock;
    private double remainingToPay;

}
