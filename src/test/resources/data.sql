INSERT INTO puretherapie.PersonOrigin (puretherapie.PersonOrigin.type)
VALUES ('None'),
       ('Facebook'),
       ('Groupon'),
       ('Friend');

INSERT INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('BOSS');
SET @boss_role_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('SECRETARY');
SET @secretary_role_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('MAMY');
SET @mamy_role_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('TECHNICIAN');
SET @technician_role_id = LAST_INSERT_ID();

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('ALL_ROLES');
SET @all_roles_level = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @all_roles_level),
       (@secretary_role_id, @all_roles_level),
       (@mamy_role_id, @all_roles_level),
       (@technician_role_id, @all_roles_level);

INSERT INTO puretherapie.notificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS');
SET @boss_level = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_level);

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY');
SET @boss_secretary_level = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_level),
       (@secretary_role_id, @boss_secretary_level);

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY_MAMY');
SET @boss_secretary_mamy_level = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_mamy_level),
       (@secretary_role_id, @boss_secretary_mamy_level),
       (@mamy_role_id, @boss_secretary_mamy_level);

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY_MAMY_TECHNICIAN');
SET @boss_secretary_mamy_technician_level = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_mamy_technician_level),
       (@secretary_role_id, @boss_secretary_mamy_technician_level),
       (@mamy_role_id, @boss_secretary_mamy_technician_level),
       (@technician_role_id, @boss_secretary_mamy_technician_level);

SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin
FROM puretherapie.PersonOrigin
WHERE type = 'None';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'boss', 'boss', 'boss@email.fr', 0, '+33 6 07 07 07 08', NOW(), @none_person_origin_id);
SET @boss_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@boss_id, 'boss', 'boss');

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'secretary', 'secretary', 'secretary@email.fr', 0, '+33 6 07 07 07 09', NOW(), @none_person_origin_id);
SET @secretary_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@secretary_id, 'secretary', 'secretary');

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'mamy', 'mamy', 'mamy@email.fr', 0, '+33 6 07 07 07 10', NOW(), @none_person_origin_id);
SET @mamy_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@mamy_id, 'mamy', 'mamy');

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'technician', 'technician', 'technician@email.fr', 0, '+33 6 07 07 07 11', NOW(), @none_person_origin_id);
SET @technician_id = LAST_INSERT_ID();

INSERT INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@technician_id, 'technician', 'technician');

INSERT INTO puretherapie.AssociationUserRole (puretherapie.AssociationUserRole.idPerson, puretherapie.AssociationUserRole.idRole)
VALUES (@boss_id, @boss_role_id),
       (@secretary_id, @secretary_role_id),
       (@mamy_id, @mamy_role_id),
       (@technician_id, @technician_role_id);
