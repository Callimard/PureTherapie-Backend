package puretherapie.crm.api.v1.historical.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalViewDTO {

    private int idHistoricalView;
    private boolean viewed;
    private HistoricalDTO historical;

}
