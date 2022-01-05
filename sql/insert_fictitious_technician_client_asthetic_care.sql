SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin FROM puretherapie.PersonOrigin WHERE type = 'None';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('C', 'client', 'client', 'client@email.fr', 0, '+33 6 06 07 06 45', NOW(), @none_person_origin_id);
SET @client_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Client (puretherapie.Client.idPerson)
VALUES (@client_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'tech', 'tech', 'tech@email.fr', 0, '+33 6 06 06 07 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('Fictitious Aesthetic Care', 39.99, 40);