package puretherapie.crm.data.reporting;

import lombok.*;
import puretherapie.crm.api.v1.reporting.controller.dto.ReportTypeDTO;
import puretherapie.crm.data.Transformable;
import puretherapie.crm.data.kpi.Kpi;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ReportType")
public class ReportType implements Transformable<ReportTypeDTO> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReportType", nullable = false)
    private int idReportType;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @ManyToMany
    @JoinTable(name = "DefaultReportTypeConfiguration",
            joinColumns = @JoinColumn(name = "idReportType"),
            inverseJoinColumns = @JoinColumn(name = "idKPI"))
    @ToString.Exclude
    private Set<Kpi> configurationKpis = new LinkedHashSet<>();

    @Override
    public ReportTypeDTO transform() {
        return ReportTypeDTO.builder()
                .idReportType(idReportType)
                .name(name)
                .configurationKpis(configurationKpis != null ? configurationKpis.stream().map(Kpi::transform).collect(Collectors.toSet()) : null)
                .build();
    }

    public BasicReportType typeToEnum() {
        return BasicReportType.valueOf(getName().toUpperCase());
    }

    // Enum.

    public enum BasicReportType {
        DAY, WEEK, MONTH, TRIMESTER, SEMESTER, YEAR, CUSTOM;

        public String reportTypeName() {
            return name().toLowerCase();
        }
    }
}