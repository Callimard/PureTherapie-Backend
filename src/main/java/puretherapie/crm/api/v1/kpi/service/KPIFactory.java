package puretherapie.crm.api.v1.kpi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class KPIFactory {

    // Constants.

    // Variables.

    private final ApplicationContext applicationContext;

    // Methods.

    /**
     * @param kpiName the kpi name
     *
     * @return the {@link ExecutableKPI} associated to the name. If no {@code ExecutableKPI} is found, returns null.
     */
    public ExecutableKPI getExecutableKPI(String kpiName) {
        return applicationContext.getBean(kpiName, ExecutableKPI.class);
    }
}
