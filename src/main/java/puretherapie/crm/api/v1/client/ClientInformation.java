package puretherapie.crm.api.v1.client;

import lombok.Builder;

import java.time.LocalDate;


@Builder
public record ClientInformation(String photo, String comment, String technicalComment,
                                String firstName, String lastName, String mail, boolean gender,
                                LocalDate birthday, String phone, String origin) {

}
