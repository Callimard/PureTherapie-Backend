package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
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
@Table(name = "BundlePurchase")
public class BundlePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBundlePurchase", nullable = false)
    private Integer idBundlePurchase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBundle", nullable = false)
    private Bundle bundle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBill", nullable = false)
    private Bill bill;
}