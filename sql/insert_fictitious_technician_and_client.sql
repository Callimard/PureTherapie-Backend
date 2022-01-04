INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('C', 'client', 'client', 'boss@email.fr', 0, '+33 6 06 07 07 00', NOW(), 1);
SET @client_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Client (puretherapie.Client.idPerson)
VALUES (@client_id);

INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'tech', 'tech', 'tech@email.fr', 0, '+33 6 06 06 07 00', NOW(), 1);
SET @tech_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);