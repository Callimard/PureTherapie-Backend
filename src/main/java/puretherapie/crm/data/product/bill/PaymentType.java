package puretherapie.crm.data.product.bill;

import lombok.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.PaymentTypeDTO;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PaymentType")
public class PaymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPaymentType", nullable = false)
    private Integer idPaymentType;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public PaymentTypeDTO transform() {
        return PaymentTypeDTO.builder()
                .idPaymentType(idPaymentType)
                .name(name)
                .description(description)
                .build();
    }
}