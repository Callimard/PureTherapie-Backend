package puretherapie.crm.data.agenda;

import lombok.*;
import puretherapie.crm.api.v1.agenda.controller.dto.GlobalOpeningTimeDTO;

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

    @Column(name = "dayNumber", nullable = false)
    private Integer dayNumber;

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

    public GlobalOpeningTimeDTO transform() {
        return GlobalOpeningTimeDTO.builder()
                .idGlobalOpeningTime(idGlobalOpeningTime)
                .dayNumber(dayNumber)
                .openingTime(openingTime != null ? openingTime.toString() : null)
                .closeTime(closeTime != null ? closeTime.toString() : null)
                .build();
    }
}