package puretherapie.crm.data.historical;

import lombok.*;
import puretherapie.crm.api.v1.historical.controller.dto.HistoricalDTO;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Historical")
public class Historical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistorical", nullable = false)
    private Integer idHistorical;

    @Column(name = "type", nullable = false)
    private Boolean type;

    @Column(name = "historicalTitle", nullable = false, length = 125)
    private String historicalTitle;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "creationDate", nullable = false)
    private LocalDateTime creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idHistoricalLevel", nullable = false)
    private HistoricalLevel historicalLevel;

    public HistoricalDTO transform() {
        return HistoricalDTO.builder()
                .idHistorical(idHistorical)
                .type(type)
                .historicalTitle(historicalTitle)
                .text(text)
                .creationDate(creationDate != null ? creationDate.toString() : null)
                .build();
    }
}