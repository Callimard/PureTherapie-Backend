package puretherapie.crm.api.v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class SimpleService {

    // Constants.

    public static final String DATA_DIRECTORY_PATH = "src/main/resources/data";

    // Errors.

    public static final String UNKNOWN_ERROR = "unknown_error";

    // Methods.

    public abstract String getSuccessTag();

    public abstract String getFailTag();

    protected Map<String, Object> generateSuccessRes() {
        return Collections.singletonMap(getSuccessTag(), "Service success");
    }

    protected Map<String, Object> generateErrorRes(Exception e) {
        if (e instanceof ServiceException serviceException)
            return Collections.singletonMap(getFailTag(), serviceException.getErrors());
        else
            return Collections.singletonMap(getFailTag(), Collections.singletonMap(UNKNOWN_ERROR, e.getMessage()));
    }

    public boolean hasSuccess(Map<String, Object> res) {
        return res.containsKey(getSuccessTag());
    }

    // Tool methods.

    public static Map<String, String> generateError(String errorName, String errorMessage) {
        Map<String, String> errors = new HashMap<>();
        errors.put(errorName, errorMessage);
        return errors;
    }

    public static void createDataDirectory() throws IOException {
        Files.createDirectories(Path.of(DATA_DIRECTORY_PATH));
    }

    // Exceptions.

    public static class ServiceException extends RuntimeException {

        private final Map<String, String> errors;

        public ServiceException(String message, Map<String, String> errors) {
            super(message);
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }

}
