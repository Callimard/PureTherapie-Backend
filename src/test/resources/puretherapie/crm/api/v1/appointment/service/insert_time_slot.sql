SELECT @technician_id := Person.idPerson
FROM puretherapie.Person
WHERE email = 'technician@email.fr';

INSERT IGNORE INTO puretherapie.TimeSlot (puretherapie.TimeSlot.day, puretherapie.TimeSlot.begin, puretherapie.TimeSlot.time,
                                          puretherapie.TimeSlot.free, puretherapie.TimeSlot.idTechnician)
VALUES ('2022-01-01', '10:15:00', 40, 0, @technician_id),
       ('2022-01-01', '12:00:00', 40, 0, @technician_id),
       ('2022-01-01', '13:00:00', 40, 0, @technician_id),
       ('2022-01-01', '13:40:00', 40, 0, @technician_id);