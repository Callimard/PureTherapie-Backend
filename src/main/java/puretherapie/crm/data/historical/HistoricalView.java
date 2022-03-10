package puretherapie.crm.data.historical;

import lombok.*;
import puretherapie.crm.api.v1.historical.controller.dto.HistoricalViewDTO;
import puretherapie.crm.data.person.user.User;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HistoricalView")
public class HistoricalView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistoricalView", nullable = false)
    private Integer idHistoricalView;

    @Column(name = "viewed", nullable = false)
    private Boolean viewed;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idHistorical", nullable = false)
    private Historical historical;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    private User user;

    public HistoricalViewDTO transform() {
        return HistoricalViewDTO.builder()
                .idHistoricalView(idHistoricalView)
                .viewed(viewed)
                .historical(historical != null ? historical.transform() : null)
                .build();
    }
}