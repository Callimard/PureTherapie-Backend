package puretherapie.crm.api.v1.agenda.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.agenda.TimeSlotAtom;
import puretherapie.crm.data.agenda.repository.TimeSlotAtomRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TimeSlotAtomService {

    // Variables.

    private final TimeSlotAtomRepository tsaRepository;

    // Methods.

    public TimeSlotAtom searchCorrectTSA( LocalDate day) {
        List<TimeSlotAtom> tsaList = tsaRepository.findAllByOrderByEffectiveDate();
        verifyTSAList(tsaList);
        return searchCorrectTSA(tsaList, day, tsaList.get(0));
    }

    private void verifyTSAList(List<TimeSlotAtom> tsaList) {
        if (tsaList == null || tsaList.isEmpty())
            throw new IllegalArgumentException("TimeSlotAtom list is null");
    }

    private TimeSlotAtom searchCorrectTSA(List<TimeSlotAtom> tsaList, LocalDate day, TimeSlotAtom chosenTSA) {
        if (!chosenTSA.getEffectiveDate().isBefore(day))
            for (int i = 1; i < tsaList.size(); i++) {
                TimeSlotAtom current = tsaList.get(i);
                if (current.getEffectiveDate().isBefore(day))
                    return current;
            }

        return chosenTSA;
    }

}
