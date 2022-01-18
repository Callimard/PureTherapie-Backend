package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AestheticCareDTO {
    private int idAestheticCare;
    private String name;
    private double price;
    private int timeExecution;
}
