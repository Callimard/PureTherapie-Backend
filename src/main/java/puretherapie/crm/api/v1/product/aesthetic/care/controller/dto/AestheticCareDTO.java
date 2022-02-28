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
    private int executionTime;
    private boolean active;

    public AestheticCare transform() {
        return AestheticCare.builder()
                .idAestheticCare(idAestheticCare)
                .name(name)
                .price(price)
                .executionTime(executionTime)
                .active(active)
                .build();
    }
}
