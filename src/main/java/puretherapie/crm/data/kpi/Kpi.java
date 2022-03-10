package puretherapie.crm.data.kpi;

import lombok.*;
import puretherapie.crm.api.v1.kpi.conrtoller.dto.KpiDTO;
import puretherapie.crm.data.Transformable;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "KPI")
public class Kpi implements Transformable<KpiDTO> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKPI", nullable = false)
    private int idKPI;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "formula")
    private String formula;

    @Column(name = "tags")
    private String tags;

    @Override
    public KpiDTO transform() {
        return KpiDTO.builder()
                .idKPI(idKPI)
                .name(name)
                .description(description)
                .formula(formula)
                .tags(tags)
                .build();
    }
}