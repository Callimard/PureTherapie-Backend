package puretherapie.crm.api.v1.product.bill.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDTO {

    private String type;
    private String date;
    private double price;
    private boolean totallyPaid;

}
