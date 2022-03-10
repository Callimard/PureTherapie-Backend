package puretherapie.crm.api.v1.historical.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDTO {

    private int idHistorical;
    private boolean type;
    private String historicalTitle;
    private String text;
    private String creationDate;

}
