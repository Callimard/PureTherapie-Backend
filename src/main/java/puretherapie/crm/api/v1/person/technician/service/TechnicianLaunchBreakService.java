package puretherapie.crm.api.v1.person.technician.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.LaunchBreakConfiguration;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.LaunchBreakRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static puretherapie.crm.tool.TimeTool.isInTZ;

@Slf4j
@AllArgsConstructor
@Service
public class TechnicianLaunchBreakService {

    // Variables.

    private final TechnicianRepository technicianRepository;
    private final LaunchBreakRepository launchBreakRepository;
    private final LaunchBreakConfiguration launchBreakConfiguration;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTechnicianLaunchBreak(int idTechnician, LocalDate day, LocalTime beginHour, int duration) {
        try {
            Technician technician = technicianRepository.findByIdPerson(idTechnician);
            LaunchBreak launchBreak = buildLaunchBreak(day, beginHour, duration, technician);
            launchBreak = launchBreakRepository.save(launchBreak);
            log.info("Launch break save => {}", launchBreak);
        } catch (Exception e) {
            log.error("Fail to create launch break for the technician {} at day {}", idTechnician, day);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    public boolean isDuringTechnicianLaunchBreak(Technician technician, LocalDate day, LocalTime begin, int duration) {
        LaunchBreak launchBreak = launchBreakRepository.findByTechnicianAndDay(technician, day);
        return isDuringTechnicianLaunchBreak(launchBreak, begin, duration);
    }

    public boolean isDuringTechnicianLaunchBreak(LaunchBreak launchBreak, LocalTime begin, int duration) {
        if (launchBreak != null) {
            return isInTZ(launchBreak.getBeginHour(), launchBreak.getBeginHour().plusMinutes(launchBreak.getDuration()), begin, duration);
        } else
            return false;
    }

    public boolean isInLaunchBreakTimeZone(LocalTime begin, int duration) {
        LocalTime launchBreakTZStart = LocalTime.parse(launchBreakConfiguration.getStart());
        LocalTime launchBreakTZEnd = LocalTime.parse(launchBreakConfiguration.getEnd());

        return isInTZ(launchBreakTZStart, launchBreakTZEnd, begin, duration);
    }

    private LaunchBreak buildLaunchBreak(LocalDate day, LocalTime beginHour, int duration, Technician technician) {
        return LaunchBreak.builder()
                .day(day)
                .duration(duration)
                .beginHour(beginHour)
                .technician(technician)
                .build();
    }

}
