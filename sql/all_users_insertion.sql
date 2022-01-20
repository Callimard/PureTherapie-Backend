SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin FROM puretherapie.PersonOrigin WHERE type = 'Aucune';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'boss', 'boss', 'boss@email.fr', 0, '+33 6 07 07 07 08', NOW(), @none_person_origin_id);
SET @boss_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@boss_id, 'boss', 'boss');

SELECT @boss_role_id := Role.idRole
FROM puretherapie.Role
WHERE roleName = 'BOSS';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'secretary', 'secretary', 'secretary@email.fr', 0, '+33 6 07 07 07 09', NOW(), @none_person_origin_id);
SET @secretary_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@secretary_id, 'secretary', 'secretary');

SELECT @secretary_role_id := Role.idRole
FROM puretherapie.Role
WHERE roleName = 'SECRETARY';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'mamy', 'mamy', 'mamy@email.fr', 0, '+33 6 07 07 07 10', NOW(), @none_person_origin_id);
SET @mamy_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@mamy_id, 'mamy', 'mamy');

SELECT @mamy_role_id := Role.idRole
FROM puretherapie.Role
WHERE roleName = 'MAMY';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'technician', 'technician', 'technician@email.fr', 0, '+33 6 07 07 07 11', NOW(), @none_person_origin_id);
SET @technician_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@technician_id, 'technician', 'technician');

SELECT @technician_role_id := Role.idRole
FROM puretherapie.Role
WHERE roleName = 'TECHNICIAN';

INSERT INTO puretherapie.AssociationUserRole (puretherapie.AssociationUserRole.idPerson, puretherapie.AssociationUserRole.idRole)
VALUES (@boss_id, @boss_role_id),
       (@secretary_id, @secretary_role_id),
       (@mamy_id, @mamy_role_id),
       (@technician_id, @technician_role_id);
