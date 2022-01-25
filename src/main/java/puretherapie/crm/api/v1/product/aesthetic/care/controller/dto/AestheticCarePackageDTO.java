package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AestheticCarePackageDTO {

    private int idAestheticCarePackage;
    private AestheticCareDTO aestheticCare;
    private String name;
    private int numberAestheticCare;

}
