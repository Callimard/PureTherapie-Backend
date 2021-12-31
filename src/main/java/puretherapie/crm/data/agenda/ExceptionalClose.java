package puretherapie.crm.data.agenda;

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
@Table(name = "ExceptionalClose")
public class ExceptionalClose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExceptionalClose", nullable = false)
    private Integer id;

    @Column(name = "day", nullable = false)
    private LocalDate day;
}