package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;

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

    public AestheticCarePackage transform() {
        return AestheticCarePackage.builder()
                .idAestheticCarePackage(idAestheticCarePackage)
                .aestheticCare(aestheticCare.transform())
                .name(name)
                .numberAestheticCare(numberAestheticCare)
                .build();
    }

}
