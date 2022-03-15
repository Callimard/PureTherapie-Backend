package puretherapie.crm.api.v1.kpi.conrtoller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {

    private int idKPI;
    private String name;
    private String description;
    private String formula;
    private String tags;

}
