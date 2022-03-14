package puretherapie.crm.api.v1.reporting.controller.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportPackageDTO {

    private List<ReportDTO> dailyReports;
    private List<ReportDTO> weeklyReports;
    private List<ReportDTO> monthlyReports;
    private List<ReportDTO> annualReports;
    private List<ReportDTO> otherReports;

}
