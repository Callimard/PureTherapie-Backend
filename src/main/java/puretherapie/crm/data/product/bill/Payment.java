package puretherapie.crm.data.product.bill;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPayment", nullable = false)
    private Integer idPayment;

    @Column(name = "amountPaid", nullable = false)
    private Double amountPaid;

    @Column(name = "paymentDate", nullable = false)
    private OffsetDateTime paymentDate;

    @ManyToOne
    @JoinColumn(name = "idBill", nullable = false)
    private Bill bill;
}