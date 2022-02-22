package puretherapie.crm.api.v1.product.aesthetic.care.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareProvisionDTO;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;

import static puretherapie.crm.api.v1.product.aesthetic.care.controller.AestheticCareProvisionController.AESTHETIC_CARE_PROVISIONS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AESTHETIC_CARE_PROVISIONS_URL)
public class AestheticCareProvisionController {

    // Constants.

    public static final String AESTHETIC_CARE_PROVISIONS_URL = "/aesthetic_care_provisions";

    public static final String AESTHETIC_CARE_PROVISIONS_BY_APPOINTMENT = "/appointment";
    public static final String AESTHETIC_CARE_PROVISIONS_BY_APPOINTMENT_URL =
            AESTHETIC_CARE_PROVISIONS_URL + AESTHETIC_CARE_PROVISIONS_BY_APPOINTMENT;

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final AestheticCareProvisionRepository aestheticCareProvisionRepository;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(AESTHETIC_CARE_PROVISIONS_BY_APPOINTMENT + "/{idAppointment}")
    public ResponseEntity<AestheticCareProvisionDTO> getACProvisionForAppointment(@PathVariable(name = "idAppointment") int idAppointment) {
        try {
            Appointment appointment = verifyAppointment(idAppointment);
            return ResponseEntity.ok(aestheticCareProvisionRepository.findByAppointment(appointment).transform());
        } catch (Exception e) {
            log.error("Fail to get ac provision for the appointment. Error message = {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new IllegalArgumentException("Unknown appointment id");

        return null;
    }
}
