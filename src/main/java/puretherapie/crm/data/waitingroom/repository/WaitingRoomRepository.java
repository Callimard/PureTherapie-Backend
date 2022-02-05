package puretherapie.crm.data.waitingroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.waitingroom.WaitingRoom;

import java.util.List;

@Repository
public interface WaitingRoomRepository extends JpaRepository<WaitingRoom, Integer> {

    WaitingRoom findByClient(Client client);

    List<WaitingRoom> findAll();

}