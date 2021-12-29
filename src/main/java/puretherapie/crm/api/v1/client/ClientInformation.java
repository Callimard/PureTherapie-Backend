package puretherapie.crm.api.v1.client;

import lombok.Builder;

import java.sql.Date;


@Builder
public record ClientInformation(String photo, String comment, String technicalComment,
                                String firstName, String lastName, String mail, boolean gender,
                                Date birthday, String phone, String origin) {

}
