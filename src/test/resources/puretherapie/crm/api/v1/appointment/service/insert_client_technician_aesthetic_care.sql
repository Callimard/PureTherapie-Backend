INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('C', 'client', 'client', 'client@email.fr', 0, '+33 6 07 27 14 40', NOW(), 1);
SET @client_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Client (puretherapie.Client.idPerson)
VALUES (@client_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'technician', 'technician', 'tech@email.fr', 0, '+33 6 07 27 14 41', NOW(), 1);
SET @technician_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@technician_id);

INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('AC', 39.99, 40);