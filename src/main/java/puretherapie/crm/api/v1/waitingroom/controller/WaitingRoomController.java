package puretherapie.crm.api.v1.waitingroom.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.waitingroom.controller.dto.WaitingRoomDTO;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.waitingroom.controller.WaitingRoomController.WAITING_ROOM_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(WAITING_ROOM_URL)
public class WaitingRoomController {

    // Constants.

    public static final String WAITING_ROOM_URL = API_V1_URL + "/waiting_room";

    // Variables.

    private final WaitingRoomRepository waitingRoomRepository;

    // Methods

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY', 'ROLE_TECHNICIAN')")
    @GetMapping
    public List<WaitingRoomDTO> getAllWaitingRoomRow() {
        List<WaitingRoom> wr = waitingRoomRepository.findAll();
        return wr.stream().map(WaitingRoom::transform).toList();
    }

}
