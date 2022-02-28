package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

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

    public AestheticCare transform() {
        return AestheticCare.builder()
                .idAestheticCare(idAestheticCare)
                .name(name)
                .price(price)
                .timeExecution(timeExecution)
                .build();
    }
}
