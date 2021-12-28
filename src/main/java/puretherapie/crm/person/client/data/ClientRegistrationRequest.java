package puretherapie.crm.person.client.data;

import lombok.Builder;

import java.sql.Date;


@Builder
public record ClientRegistrationRequest(String photo, String comment, String technicalComment,
                                        String firstName, String lastName, String mail, boolean gender,
                                        Date birthday, String phone, String origin) {

}
