package puretherapie.crm.api.v1.product.aesthetic.care.controller.parameter;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AestheticCareCreationParameter {

    private String name;
    private double price;
    private int executionTime;

}
