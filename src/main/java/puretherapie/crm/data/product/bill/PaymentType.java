package puretherapie.crm.data.product.bill;

import lombok.*;

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
    private Integer id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;
}