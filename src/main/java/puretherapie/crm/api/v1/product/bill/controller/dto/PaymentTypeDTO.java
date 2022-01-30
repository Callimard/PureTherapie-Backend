package puretherapie.crm.api.v1.product.bill.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTypeDTO {

    private int idPaymentType;
    private String name;
    private String description;

}
