package puretherapie.crm.data.agenda;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GlobalOpeningTime")
public class GlobalOpeningTime implements Opening {

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGlobalOpeningTime", nullable = false)
    private Integer idGlobalOpeningTime;

    @Column(name = "day", nullable = false)
    private Integer day;

    @Column(name = "openingTime", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closeTime", nullable = false)
    private LocalTime closeTime;

    @Override
    public LocalTime openingTime() {
        return getOpeningTime();
    }

    @Override
    public LocalTime closeTime() {
        return getCloseTime();
    }
}