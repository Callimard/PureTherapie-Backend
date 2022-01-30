package puretherapie.crm.data.product.bill;

import lombok.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

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

    @OneToMany(targetEntity = Payment.class, mappedBy = "bill")
    @ToString.Exclude
    private List<Payment> payments;

    public BillDTO transform() {
        BillDTO billDTO = BillDTO.builder()
                .idBill(idBill)
                .basePrice(basePrice)
                .purchasePrice(purchasePrice)
                .creationDate(creationDate != null ? creationDate.toString() : null)
                .client(client != null ? client.transform() : null)
                .paymentType(paymentType != null ? paymentType.transform() : null)
                .build();
        billDTO.setPayments(payments != null ? payments.stream().map(p -> p.transformWithBill(billDTO)).toList() : null);
        return billDTO;
    }
}