package puretherapie.crm.api.v1.client;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDate;


@Builder
public record ClientInformation(String photo, String comment, String technicalComment,
                                @NonNull String firstName, @NonNull String lastName, @NonNull String mail, @NonNull boolean gender,
                                LocalDate birthday, @NonNull String phone, @NonNull long idOrigin) {

}
