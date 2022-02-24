package puretherapie.crm.data.product.bill;

import lombok.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.PaymentDTO;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime paymentDate;

    @Column(name = "canceled", nullable = false)
    private boolean canceled = false;

    @ManyToOne
    @JoinColumn(name = "idMeansOfPayment", nullable = false)
    private MeansOfPayment meansOfPayment;

    @ManyToOne
    @JoinColumn(name = "idBill", nullable = false)
    private Bill bill;

    public PaymentDTO transform() {
        return PaymentDTO.builder()
                .idPayment(idPayment)
                .amountPaid(amountPaid)
                .canceled(canceled)
                .paymentDate(paymentDate != null ? paymentDate.toString() : null)
                .meansOfPayment(meansOfPayment != null ? meansOfPayment.transform() : null)
                .bill(bill != null ? bill.transform() : null)
                .build();
    }

    public PaymentDTO transformWithBill(BillDTO bill) {
        return PaymentDTO.builder()
                .idPayment(idPayment)
                .amountPaid(amountPaid)
                .canceled(canceled)
                .paymentDate(paymentDate != null ? paymentDate.toString() : null)
                .meansOfPayment(meansOfPayment != null ? meansOfPayment.transform() : null)
                .bill(bill)
                .build();
    }
}