SELECT @technician_id := Person.idPerson
FROM puretherapie.Person
WHERE email = 'tech@email.fr';

SELECT @client_id := Person.idPerson
FROM puretherapie.Person
WHERE email = 'client@email.fr';

SELECT @ac_id := AestheticCare.idAestheticCare
FROM puretherapie.AestheticCare
WHERE name = 'AC';

INSERT INTO puretherapie.Appointment (puretherapie.Appointment.idAestheticCare, puretherapie.Appointment.idClient,
                                      puretherapie.Appointment.idTechnician)
VALUES (@ac_id, @client_id, @technician_id);
SET @appointment_id = LAST_INSERT_ID();

INSERT INTO puretherapie.TimeSlot (puretherapie.TimeSlot.day, puretherapie.TimeSlot.begin, puretherapie.TimeSlot.time, puretherapie.TimeSlot
    .idTechnician, puretherapie.TimeSlot.idAppointment)
VALUES ('2022-01-01', '10:15:00', 40, @technician_id, @appointment_id),
       ('2022-01-01', '12:00:00', 40, @technician_id, @appointment_id),
       ('2022-01-01', '13:00:00', 40, @technician_id, @appointment_id),
       ('2022-01-01', '13:40:00', 40, @technician_id, @appointment_id);