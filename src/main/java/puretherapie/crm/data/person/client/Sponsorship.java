package puretherapie.crm.data.person.client;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Sponsorship")
public class Sponsorship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSponsorship", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClientSponsored", nullable = false)
    private Client clientSponsored;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClientSponsor", nullable = false)
    private Client clientSponsor;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "sponsoredUsed", nullable = false)
    private Integer sponsoredUsed;

    @Column(name = "sponsorUsed", nullable = false)
    private Integer sponsorUsed;
}