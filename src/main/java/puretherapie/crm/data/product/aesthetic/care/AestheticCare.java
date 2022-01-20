package puretherapie.crm.data.product.aesthetic.care;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AestheticCare")
public class AestheticCare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAestheticCare", nullable = false)
    private Integer idAestheticCare;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "timeExecution", nullable = false)
    private Integer timeExecution;

    public AestheticCareDTO transform() {
        return AestheticCareDTO.builder()
                .idAestheticCare(idAestheticCare)
                .name(name)
                .price(price)
                .timeExecution(timeExecution)
                .build();
    }
}