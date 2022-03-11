package puretherapie.crm.data.reporting;

import lombok.*;
import puretherapie.crm.api.v1.reporting.controller.dto.ReportDTO;
import puretherapie.crm.data.Transformable;
import puretherapie.crm.data.kpi.Kpi;

import javax.persistence.*;
import java.time.LocalDate;
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
@Table(name = "Report")
public class Report implements Transformable<ReportDTO> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReport", nullable = false)
    private int idReport;

    @Column(name = "dateBegin", nullable = false)
    private LocalDate dateBegin;

    @Column(name = "dateEnd", nullable = false)
    private LocalDate dateEnd;

    @Column(name = "file")
    private String file;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idReportType", nullable = false)
    private ReportType reportType;

    @ManyToMany
    @JoinTable(name = "ReportConfiguration",
            joinColumns = @JoinColumn(name = "idReport"),
            inverseJoinColumns = @JoinColumn(name = "idKPI"))
    @ToString.Exclude
    private Set<Kpi> configurationKpis = new LinkedHashSet<>();

    @Override
    public ReportDTO transform() {
        return ReportDTO.builder()
                .idReport(idReport)
                .dateBegin(dateBegin != null ? dateBegin.toString() : null)
                .dateEnd(dateEnd != null ? dateEnd.toString() : null)
                .file(file)
                .reportType(reportType != null ? reportType.transform() : null)
                .configurationKpis(configurationKpis != null ? configurationKpis.stream().map(Kpi::transform).collect(Collectors.toSet()) : null)
                .build();
    }
}