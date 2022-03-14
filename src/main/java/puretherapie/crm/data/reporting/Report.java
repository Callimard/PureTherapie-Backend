package puretherapie.crm.data.reporting;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.api.v1.kpi.service.KPIFactory;
import puretherapie.crm.api.v1.reporting.controller.dto.ReportDTO;
import puretherapie.crm.data.Transformable;
import puretherapie.crm.data.kpi.Kpi;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Report")
public class Report implements Transformable<ReportDTO> {

    // Static.

    public static final String REPORT_ROOT_PATH = "/reports";

    // Variables.

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

    public List<ExecutableKPI.KPIRes> execute(KPIFactory kpiFactory) {
        List<ExecutableKPI.KPIRes> kpiResList = new ArrayList<>();
        for (Kpi kpi : configurationKpis) {
            ExecutableKPI executableKPI = kpiFactory.getExecutableKPI(kpi.getName());
            kpiResList.add(executableKPI.execute(dateBegin, dateEnd));
        }
        return kpiResList;
    }
}