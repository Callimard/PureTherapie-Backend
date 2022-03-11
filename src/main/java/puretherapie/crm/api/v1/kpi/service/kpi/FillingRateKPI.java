package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.kpi.service.kpi.FillingRateKPI.FILLING_RATE_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(FILLING_RATE_KPI_NAME)
public class FillingRateKPI extends ExecutableKPI {

    // Constants.

    public static final String FILLING_RATE_KPI_NAME = "FillingRateKPI";

    // Variables

    private final OpeningService openingService;
    private final TechnicianService technicianService;
    private final TechnicianRepository technicianRepository;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        List<Technician> technicians = technicianRepository.findByActive(true);

        Map<String, Integer> mapTechAvailableTS = new HashMap<>();
        Map<String, Integer> mapTechAppointmentTS = new HashMap<>();
        fillTechTSMap(technicians, mapTechAvailableTS, mapTechAppointmentTS);

        int totalAvailableTS = 0;
        int totalAppointmentTS = 0;
        for (LocalDate ite = begin; ite.isBefore(end.plusDays(1)); ite = ite.plusDays(1)) {
            for (Technician technician : technicians) {
                List<TimeSlotDTO> technicianTSDay = technicianService.getTechnicianTimeSlotOfTheDay(technician.getIdPerson(), ite);
                if (!technicianTSDay.isEmpty()) {
                    int totalTechAvailableTS = nbAvailableTS(technicianTSDay);
                    int totalTechAppointmentTS = nbAppointmentTS(technicianTSDay);

                    updateMapTechTS(mapTechAvailableTS, mapTechAppointmentTS, technician.simplyIdentifier(), totalTechAvailableTS,
                                    totalTechAppointmentTS);

                    totalAvailableTS += totalTechAvailableTS;
                    totalAppointmentTS += totalTechAppointmentTS;
                }
            }
        }

        double totalFillingRate;
        if (totalAvailableTS > 0) {
            totalFillingRate = (double) totalAppointmentTS / (double) totalAvailableTS;
        } else {
            totalFillingRate = -1.d;
        }

        List<TechnicianFillingRate> technicianFillingRates =
                getTechnicianFillingRates(technicians, mapTechAvailableTS, mapTechAppointmentTS);

        return new FillingRateKPIRes(totalFillingRate, technicianFillingRates);
    }

    private void fillTechTSMap(List<Technician> technicians, Map<String, Integer> mapTechAvailableTS, Map<String, Integer> mapTechAppointmentTS) {
        for (Technician technician : technicians) {
            mapTechAvailableTS.put(technician.simplyIdentifier(), 0);
            mapTechAppointmentTS.put(technician.simplyIdentifier(), 0);
        }
    }

    private void updateMapTechTS(Map<String, Integer> mapTechAvailableTS, Map<String, Integer> mapTechAppointmentTS, String simplyIdentifier,
                                 int totalTechAvailableTS, int totalTechAppointmentTS) {
        mapTechAvailableTS.put(simplyIdentifier, mapTechAvailableTS.get(simplyIdentifier) + totalTechAvailableTS);
        mapTechAppointmentTS.put(simplyIdentifier, mapTechAppointmentTS.get(simplyIdentifier) + totalTechAppointmentTS);
    }

    private int nbAvailableTS(List<TimeSlotDTO> technicianTSDay) {
        int counter = 0;
        for (TimeSlotDTO ts : technicianTSDay) {
            if (!ts.isAbsence() && !ts.isLaunchBreak())
                counter++;
        }
        return counter;
    }

    private int nbAppointmentTS(List<TimeSlotDTO> technicianTSDay) {
        int counter = 0;
        for (TimeSlotDTO ts : technicianTSDay) {
            if (!ts.isFree())
                counter++;
        }
        return counter;
    }

    private List<TechnicianFillingRate> getTechnicianFillingRates(List<Technician> technicians, Map<String, Integer> mapTechAvailableTS,
                                                                  Map<String, Integer> mapTechAppointmentTS) {
        List<TechnicianFillingRate> technicianFillingRates = new ArrayList<>();
        technicians.forEach(tech -> {
            int totalTechAvailableTS = mapTechAvailableTS.get(tech.simplyIdentifier());
            int totalTechAppointmentTS = mapTechAppointmentTS.get(tech.simplyIdentifier());

            double techFillingRate;
            if (totalTechAvailableTS > 0) {
                techFillingRate = (double) totalTechAppointmentTS / (double) totalTechAvailableTS;
            } else {
                techFillingRate = -1.d;
            }
            technicianFillingRates.add(new TechnicianFillingRate(tech.simplyIdentifier(), techFillingRate));
        });
        return technicianFillingRates;
    }

    // Inner class.

    public static class FillingRateKPIRes extends KPIRes {
        public FillingRateKPIRes(double totalFillingRate, List<TechnicianFillingRate> technicianFillingRates) {
            super(FILLING_RATE_KPI_NAME, new FillingRateKPIPackage(totalFillingRate, technicianFillingRates));
        }
    }

    public static record FillingRateKPIPackage(double totalFillingRate, List<TechnicianFillingRate> technicianFillingRates) {
    }

    public static record TechnicianFillingRate(String technicianIdentifier, double fillingRate) {
    }
}
