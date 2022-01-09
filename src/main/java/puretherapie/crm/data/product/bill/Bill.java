package puretherapie.crm.data.product.bill;

import lombok.*;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBill", nullable = false)
    private Integer idBill;

    @Column(name = "basePrice", nullable = false)
    private Double basePrice;

    @Column(name = "purchasePrice", nullable = false)
    private Double purchasePrice;

    @Column(name = "creationDate", nullable = false)
    private OffsetDateTime creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPaymentType", nullable = false)
    private PaymentType paymentType;
}