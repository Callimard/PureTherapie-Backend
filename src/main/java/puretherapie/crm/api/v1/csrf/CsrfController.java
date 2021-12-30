package puretherapie.crm.api.v1.csrf;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static puretherapie.crm.api.v1.ApiV1.generateOkJsonResponse;
import static puretherapie.crm.api.v1.csrf.CsrfController.API_V1_CSRF_URL;

@RestController
@RequestMapping(API_V1_CSRF_URL)
public class CsrfController {

    public static final String API_V1_CSRF_URL = "/api/v1/csrf";

    @GetMapping
    public ResponseEntity<String> generateCsrf() {
        return generateOkJsonResponse("""
                                              {
                                                  "message":"csrf token generate"
                                              }
                                                """);
    }

}
