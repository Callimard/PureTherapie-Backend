package puretherapie.crm.data.product.bill;

import lombok.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.MeansOfPaymentDTO;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MeansOfPayment")
public class MeansOfPayment {

    // Constants.

    public static final String GROUPON_PAYMENT = "Groupon";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMeansOfPayment", nullable = false)
    private Integer idMeansOfPayment;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "description")
    private String description;

    public MeansOfPaymentDTO transform() {
        return MeansOfPaymentDTO.builder()
                .idMeansOfPayment(idMeansOfPayment)
                .name(name)
                .description(description)
                .build();
    }

    public boolean isGrouponPayment() {
        return name.equals(GROUPON_PAYMENT);
    }
}