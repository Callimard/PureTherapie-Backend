package puretherapie.crm.data.agenda;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public interface Opening {

    LocalTime openingTime();

    LocalTime closeTime();

    static List<LocalTime> correctTimeSlotTime(Opening opening, int tsaNumberOfMinutes) {
        if (!opening.openingTime().equals(opening.closeTime()) && !opening.openingTime().isBefore(opening.closeTime()))
            throw new IllegalArgumentException("OpeningTime not before CloseTime");
        
        List<LocalTime> correctTimeSlotTimes = new ArrayList<>();
        LocalTime timeSlot = opening.openingTime();
        do {
            correctTimeSlotTimes.add(timeSlot);
            timeSlot = timeSlot.plusMinutes(tsaNumberOfMinutes);
        } while (!timeSlot.equals(opening.closeTime()));
        correctTimeSlotTimes.add(timeSlot);

        return correctTimeSlotTimes;
    }

}
