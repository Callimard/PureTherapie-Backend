package puretherapie.crm.api.v1;

import org.springframework.http.ResponseEntity;

public class ApiV1 {

    public static final String API_V1_URL = "/api/v1";

    private ApiV1() {}

    public static ResponseEntity<String> generateOkJsonResponse(String s) {
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(s);
    }

}
