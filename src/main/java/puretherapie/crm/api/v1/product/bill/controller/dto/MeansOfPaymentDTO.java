package puretherapie.crm.api.v1.product.bill.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MeansOfPaymentDTO {

    private int idMeansOfPayment;
    private String name;
    private String description;

}
