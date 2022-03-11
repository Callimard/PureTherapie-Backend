package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static puretherapie.crm.api.v1.kpi.service.kpi.NotFinalizedAppointmentKPI.APPOINTMENT_NOT_FINALIZED_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(APPOINTMENT_NOT_FINALIZED_KPI_NAME)
public class NotFinalizedAppointmentKPI extends ExecutableKPI {

    // Constants.

    public static final String APPOINTMENT_NOT_FINALIZED_KPI_NAME = "NotFinalizedAppointmentKPI";

    // Variables.

    private final AestheticCareProvisionRepository aestheticCareProvisionRepository;

    // Methods.

    @Override

    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0, 0));

        List<AestheticCareProvision> aestheticCareProvisionList =
                aestheticCareProvisionRepository.findByDateGreaterThanEqualAndDateLessThan(beginDateTime, endDateTime);
        int counter = 0;
        for (AestheticCareProvision aestheticCareProvision : aestheticCareProvisionList) {
            if (!aestheticCareProvision.getAppointment().isFinalized()) {
                counter++;
            }
        }

        return new NotFinalizedAppointmentKPIRes(counter);
    }

    // Inner classes.

    public static class NotFinalizedAppointmentKPIRes extends KPIRes {
        public NotFinalizedAppointmentKPIRes(int nbNotFinalizedAppointment) {
            super(APPOINTMENT_NOT_FINALIZED_KPI_NAME, new NotFinalizedAppointmentKPIPackage(nbNotFinalizedAppointment));
        }
    }

    public static record NotFinalizedAppointmentKPIPackage(int nbNotFinalizedAppointment) {
    }
}
