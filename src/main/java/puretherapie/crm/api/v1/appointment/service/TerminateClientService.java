package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.SimpleService;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class TerminateClientService extends SimpleService {

    // Constants.

    public static final String TERMINATE_CLIENT_SUCCESS = "terminate_client_success";
    public static final String TERMINATE_CLIENT_FAIL = "terminate_client_fail";

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return TERMINATE_CLIENT_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return TERMINATE_CLIENT_FAIL;
    }

    // Exceptions.

    private static class TerminateClientException extends SimpleService.ServiceException {
        public TerminateClientException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
