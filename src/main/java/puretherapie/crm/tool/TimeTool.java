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

}
