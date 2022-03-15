package puretherapie.crm.api.v1.reporting.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.kpi.conrtoller.dto.KpiDTO;

import java.util.Set;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private int idReport;
    private String dateBegin;
    private String dateEnd;
    private String file;
    private ReportTypeDTO reportType;
    private Set<KpiDTO> configurationKpis;

}
