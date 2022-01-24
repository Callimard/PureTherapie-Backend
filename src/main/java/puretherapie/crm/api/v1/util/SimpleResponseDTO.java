package puretherapie.crm.api.v1.util;

import org.springframework.http.ResponseEntity;

public interface SimpleResponseDTO {

    String message();

    boolean success();

    static ResponseEntity<SimpleResponseDTO> generateResponse(SimpleResponseDTO simpleResponseDTO) {
        if (simpleResponseDTO.success()) {
            return ResponseEntity.ok(simpleResponseDTO);
        } else {
            return ResponseEntity.badRequest().body(simpleResponseDTO);
        }
    }

    static SimpleResponseDTO generateFail(String msg) {
        return new SimpleResponseDTO() {
            @Override
            public String message() {
                return msg;
            }

            @Override
            public boolean success() {
                return false;
            }
        };
    }

    static SimpleResponseDTO generateSuccess(String msg) {
        return new SimpleResponseDTO() {
            @Override
            public String message() {
                return msg;
            }

            @Override
            public boolean success() {
                return true;
            }
        };
    }

}
