package puretherapie.crm.api.v1.agenda.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.agenda.ExceptionalOpening;
import puretherapie.crm.data.agenda.GlobalOpeningTime;
import puretherapie.crm.data.agenda.Opening;
import puretherapie.crm.data.agenda.repository.ExceptionalCloseRepository;
import puretherapie.crm.data.agenda.repository.ExceptionalOpeningRepository;
import puretherapie.crm.data.agenda.repository.GlobalOpeningTimeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class OpeningService {

    // Variables.

    private final ExceptionalOpeningRepository eoRepository;
    private final ExceptionalCloseRepository ecRepository;
    private final GlobalOpeningTimeRepository gotRepository;

    // Methods.

    public boolean hasExceptionClose(LocalDate day) {
        return ecRepository.findByDay(day) != null;
    }

    public boolean isOpen(LocalDate day) {
        return ecRepository.findByDay(day) == null &&
                (!eoRepository.findByDay(day).isEmpty() || !gotRepository.findByDay(day.getDayOfWeek().getValue()).isEmpty());
    }

    public List<Opening> getOpenings(LocalDate day) {
        List<ExceptionalOpening> eoList = eoRepository.findByDay(day);
        List<GlobalOpeningTime> gotList = gotRepository.findByDay(day.getDayOfWeek().getValue());

        List<Opening> openingList = new ArrayList<>();
        openingList.addAll(eoList);
        openingList.addAll(gotList);
        return openingList;
    }

}
