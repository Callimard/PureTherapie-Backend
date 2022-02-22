package puretherapie.crm.api.v1.agenda.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.agenda.controller.dto.ExceptionalCloseDTO;
import puretherapie.crm.api.v1.agenda.controller.dto.ExceptionalOpeningDTO;
import puretherapie.crm.api.v1.agenda.controller.dto.GlobalOpeningTimeDTO;
import puretherapie.crm.data.agenda.ExceptionalClose;
import puretherapie.crm.data.agenda.ExceptionalOpening;
import puretherapie.crm.data.agenda.GlobalOpeningTime;
import puretherapie.crm.data.agenda.repository.ExceptionalCloseRepository;
import puretherapie.crm.data.agenda.repository.ExceptionalOpeningRepository;
import puretherapie.crm.data.agenda.repository.GlobalOpeningTimeRepository;

import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.agenda.controller.OpeningAndCloseController.OPENING_AND_CLOSE_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(OPENING_AND_CLOSE_URL)
public class OpeningAndCloseController {

    // Constants.

    public static final String OPENING_AND_CLOSE_URL = API_V1_URL + "/openings";

    public static final String EXCEPTIONAL_OPENINGS = "/exceptional_openings";
    public static final String EXCEPTIONAL_OPENINGS_URL = OPENING_AND_CLOSE_URL + EXCEPTIONAL_OPENINGS;

    public static final String EXCEPTIONAL_CLOSINGS = "/exceptional_closings";
    public static final String EXCEPTIONAL_CLOSINGS_URL = OPENING_AND_CLOSE_URL + EXCEPTIONAL_CLOSINGS;

    // Variables.

    private final GlobalOpeningTimeRepository globalOpeningTimeRepository;
    private final ExceptionalOpeningRepository exceptionalOpeningRepository;
    private final ExceptionalCloseRepository exceptionalCloseRepository;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping()
    public List<GlobalOpeningTimeDTO> getAllGlobalOpeningTimes() {
        return globalOpeningTimeRepository.findAll().stream().map(GlobalOpeningTime::transform).toList();
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping(EXCEPTIONAL_OPENINGS)
    public List<ExceptionalOpeningDTO> getAllExceptionalOpenings() {
        return exceptionalOpeningRepository.findAll().stream().map(ExceptionalOpening::transform).toList();
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping(EXCEPTIONAL_CLOSINGS)
    public List<ExceptionalCloseDTO> getAllExceptionalCloses() {
        return exceptionalCloseRepository.findAll().stream().map(ExceptionalClose::transform).toList();
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping
    public void addGlobalOpeningTime(@RequestBody GlobalOpeningTimeDTO globalOpeningTimeDTO) {
        GlobalOpeningTime globalOpeningTime = globalOpeningTimeDTO.transform();
        globalOpeningTime.setIdGlobalOpeningTime(0);
        globalOpeningTimeRepository.save(globalOpeningTime);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PutMapping
    public void updateGlobalOpeningTime(@RequestBody GlobalOpeningTimeDTO globalOpeningTimeDTO) {
        GlobalOpeningTime globalOpeningTime = globalOpeningTimeDTO.transform();
        globalOpeningTimeRepository.save(globalOpeningTime);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @DeleteMapping("/{idGlobalOpeningTime}")
    public void deleteGlobalOpeningTime(@PathVariable(name = "idGlobalOpeningTime") int idGlobalOpeningTime) {
        globalOpeningTimeRepository.deleteById(idGlobalOpeningTime);
    }


    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(EXCEPTIONAL_OPENINGS)
    public void addExceptionalOpeningTime(@RequestBody ExceptionalOpeningDTO exceptionalOpeningDTO) {
        ExceptionalOpening exceptionalOpening = exceptionalOpeningDTO.transform();
        exceptionalOpening.setIdExceptionalOpening(0);
        exceptionalOpeningRepository.save(exceptionalOpening);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PutMapping(EXCEPTIONAL_OPENINGS)
    public void updateExceptionalOpeningTime(@RequestBody ExceptionalOpeningDTO exceptionalOpeningDTO) {
        ExceptionalOpening exceptionalOpening = exceptionalOpeningDTO.transform();
        exceptionalOpeningRepository.save(exceptionalOpening);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @DeleteMapping(EXCEPTIONAL_OPENINGS + "/{idExceptionalOpening}")
    public void deleteExceptionalOpeningTime(@PathVariable(name = "idExceptionalOpening") int idExceptionalOpening) {
        exceptionalOpeningRepository.deleteById(idExceptionalOpening);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(EXCEPTIONAL_CLOSINGS)
    public void addExceptionalOpeningTime(@RequestBody ExceptionalCloseDTO exceptionalCloseDTO) {
        ExceptionalClose exceptionalClose = exceptionalCloseDTO.transform();
        exceptionalClose.setIdExceptionalClose(0);
        exceptionalCloseRepository.save(exceptionalClose);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PutMapping(EXCEPTIONAL_CLOSINGS)
    public void updateExceptionalOpeningTime(@RequestBody ExceptionalCloseDTO exceptionalCloseDTO) {
        ExceptionalClose exceptionalClose = exceptionalCloseDTO.transform();
        exceptionalCloseRepository.save(exceptionalClose);
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @DeleteMapping(EXCEPTIONAL_CLOSINGS + "/{idExceptionalClose}")
    public void updateExceptionalOpeningTime(@PathVariable(name = "idExceptionalClose") int idExceptionalClose) {
        exceptionalCloseRepository.deleteById(idExceptionalClose);
    }

}
