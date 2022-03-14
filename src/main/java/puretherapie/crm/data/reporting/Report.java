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
import java.util.*;
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
        List<String> kpis = new ArrayList<>(configurationKpis.stream().map(Kpi::getName).toList());
        Collections.sort(kpis);
        for (String kpiName : kpis) {
            ExecutableKPI executableKPI = kpiFactory.getExecutableKPI(kpiName);
            kpiResList.add(executableKPI.execute(dateBegin, dateEnd));
        }
        return kpiResList;
    }

    public String generateReportTile() {
        return switch (getReportType().typeToEnum()) {
            case DAY -> "Rapport journalier du " + getDateBegin().toString();
            case WEEK -> "Rapport hebdomadaire de la semaine du " + getDateBegin().toString() + " au " + getDateEnd();
            case MONTH -> "Rapport mensuel du mois de" + getDateBegin().getMonth() + " de l'année " + getDateBegin().getYear();
            case TRIMESTER -> "Rapport trimestriel de l'année " + getDateBegin().getYear() + " (du " + getDateBegin().toString() + " au " +
                    getDateEnd() +
                    ")";
            case SEMESTER -> "Rapport semestriel de l'année  " + getDateBegin().getYear() + " (du " + getDateBegin().toString() + " au " +
                    getDateEnd() +
                    ")";
            case YEAR -> "Rapport annuel de l'année " + getDateBegin().getYear();
            default -> "Rapport du " + getDateBegin().toString() + " au " + getDateEnd().toString();
        };
    }
}