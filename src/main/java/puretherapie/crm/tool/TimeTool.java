package puretherapie.crm.tool;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimeTool {

    private TimeTool() {
    }

    // Tool methods.

    /**
     * @param timeBefore time before
     * @param timeAfter  time after
     *
     * @return the number of minutes between each time.
     *
     * @throws IllegalArgumentException if timeBefore is not before timeAfter
     */
    public static long minuteBetween(LocalTime timeBefore, LocalTime timeAfter) {
        if (!timeBefore.equals(timeAfter) && !timeBefore.isBefore(timeAfter))
            throw new IllegalArgumentException("TimeBefore must be before TimeAfter");

        return ChronoUnit.MINUTES.between(timeBefore, timeAfter);
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Verify if the beginning time + the duration is not in the specified TZ
     *
     * @param tzStart  tz start
     * @param tzEnd    tz end
     * @param begin    begin
     * @param duration duration
     *
     * @return true if the beginning time associated to the duration is in the TZ, else false.
     */
    public static boolean isInTZ(LocalTime tzStart, LocalTime tzEnd, LocalTime begin, int duration) {
        return beginInTZ(tzStart, tzEnd, begin) || overlapsTZ(tzStart, begin, duration);
    }

    public static boolean beginInTZ(LocalTime tzStart, LocalTime tzEnd, LocalTime begin) {
        return (begin.equals(tzStart) || begin.isAfter(tzStart)) && begin.isBefore(tzEnd);
    }

    public static boolean overlapsTZ(LocalTime tzStart, LocalTime begin, int duration) {
        LocalTime tsEnd = begin.plusMinutes(duration);
        return begin.isBefore(tzStart) && tsEnd.isAfter(tzStart);
    }

}
