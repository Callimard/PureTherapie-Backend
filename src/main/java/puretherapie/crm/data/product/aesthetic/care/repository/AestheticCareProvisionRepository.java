package puretherapie.crm.data.product.aesthetic.care.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;

import java.time.LocalDateTime;
import java.util.List;

public interface AestheticCareProvisionRepository extends JpaRepository<AestheticCareProvision, Integer> {

    AestheticCareProvision findByAppointment(Appointment appointment);

    AestheticCareProvision findByClientAndAppointment(Client client, Appointment appointment);

    List<AestheticCareProvision> findByDateGreaterThanEqualAndDateLessThan(LocalDateTime begin, LocalDateTime end);

    List<AestheticCareProvision> findByTechnicianAndDateGreaterThanEqualAndDateLessThan(Technician technician, LocalDateTime begin,
                                                                                        LocalDateTime end);

}