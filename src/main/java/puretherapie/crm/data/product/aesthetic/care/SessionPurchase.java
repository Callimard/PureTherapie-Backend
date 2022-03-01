package puretherapie.crm.data.product.aesthetic.care;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.SessionPurchaseDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.PurchaseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.bill.Bill;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SessionPurchase")
public class SessionPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSessionPurchase", nullable = false)
    private Integer idSessionPurchase;

    @Column(name = "used", nullable = false)
    private boolean used;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBill", nullable = false)
    private Bill bill;

    public SessionPurchaseDTO transform() {
        return SessionPurchaseDTO.builder()
                .idSessionPurchase(idSessionPurchase)
                .used(used)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .client(client != null ? client.transform() : null)
                .bill(bill != null ? bill.transform() : null)
                .build();
    }

    public PurchaseDTO toPurchase() {
        return PurchaseDTO.builder()
                .type("Soin")
                .date(bill.getCreationDate().toString())
                .price(bill.getPurchasePrice())
                .build();
    }

}