package puretherapie.crm.data.agenda;

import lombok.*;
import puretherapie.crm.api.v1.agenda.controller.dto.ExceptionalCloseDTO;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ExceptionalClose")
public class ExceptionalClose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExceptionalClose", nullable = false)
    private Integer idExceptionalClose;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    public ExceptionalCloseDTO transform() {
        return ExceptionalCloseDTO.builder()
                .idExceptionalClose(idExceptionalClose)
                .day(day != null ? day.toString() : null)
                .build();
    }
}