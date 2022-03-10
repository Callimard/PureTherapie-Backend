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
public class ReportTypeDTO {

    private int idReportType;
    private String name;
    private Set<KpiDTO> configurationKpis;

}
