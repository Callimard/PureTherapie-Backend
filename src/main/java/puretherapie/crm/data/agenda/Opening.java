package puretherapie.crm.data.agenda;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public interface Opening {

    LocalTime openingTime();

    LocalTime closeTime();

    default List<LocalTime> correctTimeSlotTime(TimeSlotAtom tsa) {
        if (!openingTime().equals(closeTime()) && !openingTime().isBefore(closeTime()))
            throw new IllegalArgumentException("OpeningTime not before CloseTime");

        List<LocalTime> correctTimeSlotTimes = new ArrayList<>();
        LocalTime timeSlot = openingTime();
        do {
            correctTimeSlotTimes.add(timeSlot);
            timeSlot = timeSlot.plusMinutes(tsa.getNumberOfMinutes());
        } while (!timeSlot.equals(closeTime()));
        correctTimeSlotTimes.add(timeSlot);

        return correctTimeSlotTimes;
    }

}
