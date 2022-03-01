package puretherapie.crm.data.appointment;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.SurbookingDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Surbooking")
public class Surbooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSurbooking", nullable = false)
    private int idSurbooking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

    @ManyToOne
    @JoinColumn(name = "idClientArrival")
    private ClientArrival clientArrival;

    @Column(name = "canceled", nullable = false)
    private boolean canceled = false;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "finalized", nullable = false)
    private boolean finalized;

    public SurbookingDTO transform() {
        return SurbookingDTO.builder()
                .idSurbooking(idSurbooking)
                .client(client != null ? client.transform() : null)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .clientArrival(clientArrival != null ? clientArrival.transform() : null)
                .canceled(canceled)
                .day(day != null ? day.toString() : null)
                .time(time != null ? time.toString() : null)
                .finalized(finalized)
                .build();
    }
}