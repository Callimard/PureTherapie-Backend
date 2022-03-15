package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.data.appointment.repository.SurbookingRepository;

import java.time.LocalDate;

import static puretherapie.crm.api.v1.kpi.service.kpi.SurbookingKPI.SURBOOKING_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(SURBOOKING_KPI_NAME)
public class SurbookingKPI extends ExecutableKPI {

    // Constants.

    public static final String SURBOOKING_KPI_NAME = "SurbookingKPI";

    // Variables.

    private SurbookingRepository surbookingRepository;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        return new SurbookingKPIRes(surbookingRepository.findByCanceledFalseAndDayGreaterThanEqualAndDayLessThan(begin, end.plusDays(1)).size());
    }

    public static class SurbookingKPIRes extends KPIRes {
        public SurbookingKPIRes(int nbSurbooking) {
            super(SURBOOKING_KPI_NAME, new SurbookingPackage(nbSurbooking));
        }
    }

    public static record SurbookingPackage(int nbSurbooking) {
    }
}
