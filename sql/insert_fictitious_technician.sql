SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin FROM puretherapie.PersonOrigin WHERE type = 'Aucune';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'sabine', 'deloute', 'sabine.delout@puretherapie.fr', 0, '+33 6 06 06 07 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'marine', 'courtenay', 'marine.courtenay@puretherapie.fr', 0, '+33 6 06 06 77 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'romane', 'boucher', 'romane.boucer@puretherapie.fr', 0, '+33 6 66 66 77 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'charelene', 'blanche', 'charelene.blanche@puretherapie.fr', 0, '+33 6 76 96 07 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);