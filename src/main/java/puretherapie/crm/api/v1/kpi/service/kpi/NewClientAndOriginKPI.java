package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.appointment.service.AppointmentService;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.kpi.service.kpi.NewClientAndOriginKPI.NEW_CLIENT_AND_ORIGIN_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(NEW_CLIENT_AND_ORIGIN_KPI_NAME)
public class NewClientAndOriginKPI extends ExecutableKPI {

    // Constants.

    public static final String NEW_CLIENT_AND_ORIGIN_KPI_NAME = "NewClientAndOriginKPI";

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        List<Appointment> appointments = appointmentRepository.findByDayGreaterThanEqualAndDayLessThanAndCanceledFalse(begin, end.plusDays(1));
        Map<String, Integer> mapOriginNbNewClient = new HashMap<>();

        long totalNewClients = appointments.stream().filter(app -> {
            if (appointmentService.isFirstAppointment(app)) {
                mapOriginNbNewClient.merge(app.getClient().getPersonOrigin().getType(), 1, Integer::sum);
                return true;
            } else {
                return false;
            }
        }).count();

        List<NewClientOrigin> newClientOrigins = mapOriginNbNewClient.entrySet().stream()
                .map(entry -> new NewClientOrigin(entry.getKey(), entry.getValue())).toList();

        return new NewClientAndOriginKPIRes(totalNewClients, newClientOrigins);
    }

    // Inner classes.

    public static class NewClientAndOriginKPIRes extends KPIRes {
        public NewClientAndOriginKPIRes(long totalNewClient, List<NewClientOrigin> newClientOrigins) {
            super(NEW_CLIENT_AND_ORIGIN_KPI_NAME, new NewClientAndOriginKPIPackage(totalNewClient, newClientOrigins));
        }
    }

    public static record NewClientAndOriginKPIPackage(long totalNewClient, List<NewClientOrigin> newClientOrigins) {
    }

    public static record NewClientOrigin(String origin, int nbNewClients) {
    }
}
