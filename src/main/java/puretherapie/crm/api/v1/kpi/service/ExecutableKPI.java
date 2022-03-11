package puretherapie.crm.api.v1.kpi.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public abstract class ExecutableKPI {

    // Constructor.

    protected ExecutableKPI() {
    }

    // Methods.

    /**
     * Execute the KPI in the specified date interval
     *
     * @param begin the beginning date
     * @param end   the end date
     *
     * @return the result of the execution of the KPI between specified date
     *
     * @throws IllegalArgumentException if begin is not equal or before end
     */
    public KPIRes execute(LocalDate begin, LocalDate end) {
        if (begin.isBefore(end) || begin.equals(end)) {
            return this.executeFor(begin, end);
        } else
            throw new IllegalArgumentException("KPI begin date is not before or equal to KPI end date");
    }

    protected abstract KPIRes executeFor(LocalDate begin, LocalDate end);

    // Inner class

    /**
     * Class which contains the res of a {@link ExecutableKPI}.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class KPIRes {

        // Variables.

        private final String kpiName;
        public final Object res;
    }
}
