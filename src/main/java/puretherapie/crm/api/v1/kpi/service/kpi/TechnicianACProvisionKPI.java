package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.kpi.service.kpi.TechnicianACProvisionKPI.TECHNICIAN_AC_PROVISION_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(TECHNICIAN_AC_PROVISION_KPI_NAME)
public class TechnicianACProvisionKPI extends ExecutableKPI {

    // Constants.

    public static final String TECHNICIAN_AC_PROVISION_KPI_NAME = "TechnicianACProvisionKPI";

    // Variables.

    private final TechnicianRepository technicianRepository;
    private final AestheticCareProvisionRepository acProvisionRepository;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        List<Technician> technicians = technicianRepository.findByActive(true);
        Map<String, Integer> mapTechNbAC = new HashMap<>();
        Map<String, Double> mapTechAveragePay = new HashMap<>();

        for (LocalDate ite = begin; ite.isBefore(end.plusDays(1)); ite = ite.plusDays(1)) {
            for (Technician technician : technicians) {
                LocalDateTime beginDay = LocalDateTime.of(ite, LocalTime.of(0, 0, 0));
                LocalDateTime endDay = LocalDateTime.of(ite.plusDays(1), LocalTime.of(0, 0, 0));

                List<AestheticCareProvision> techDayACProvisions =
                        acProvisionRepository.findByTechnicianAndDateGreaterThanEqualAndDateLessThan(technician, beginDay, endDay);
                mapTechNbAC.merge(technician.simplyIdentifier(), techDayACProvisions.size(), Integer::sum);

                double techDayPay =
                        techDayACProvisions.stream().map(acP -> acP.getAestheticCare().getPrice()).reduce(Double::sum).orElse(0.d);
                mapTechAveragePay.merge(technician.simplyIdentifier(), techDayPay, Double::sum);
            }
        }

        List<TechnicianNumberACProvision> technicianNumberACProvisions = mapTechNbAC.entrySet().stream()
                .map(entry -> new TechnicianNumberACProvision(entry.getKey(), entry.getValue())).toList();
        List<TechnicianAveragePay> technicianAveragePays = mapTechAveragePay.entrySet().stream()
                .map(entry -> new TechnicianAveragePay(entry.getKey(), entry.getValue() / mapTechAveragePay.size())).toList();

        return new TechnicianACProvisionKPIRes(technicianNumberACProvisions, technicianAveragePays);
    }

    public static class TechnicianACProvisionKPIRes extends KPIRes {
        public TechnicianACProvisionKPIRes(List<TechnicianNumberACProvision> technicianNumberACProvisions,
                                           List<TechnicianAveragePay> technicianAveragePays) {
            super(TECHNICIAN_AC_PROVISION_KPI_NAME, new TechnicianACProvisionKPIPackage(technicianNumberACProvisions, technicianAveragePays));
        }
    }

    public static record TechnicianACProvisionKPIPackage(List<TechnicianNumberACProvision> technicianNumberACProvisions,
                                                         List<TechnicianAveragePay> technicianAveragePays) {
    }

    public static record TechnicianNumberACProvision(String technician, int nbACProvisions) {
    }

    public static record TechnicianAveragePay(String technician, double averagePay) {
    }
}
