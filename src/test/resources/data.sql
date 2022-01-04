INSERT IGNORE INTO puretherapie.PersonOrigin (puretherapie.PersonOrigin.type)
VALUES ('None'),
       ('Facebook'),
       ('Groupon'),
       ('Friend');

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('BOSS');
SET @boss_role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('SECRETARY');
SET @secretary_role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('MAMY');
SET @mamy_role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('TECHNICIAN');
SET @technician_role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('ALL_ROLES');
SET @all_roles_level = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                                  puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @all_roles_level),
       (@secretary_role_id, @all_roles_level),
       (@mamy_role_id, @all_roles_level),
       (@technician_role_id, @all_roles_level);

INSERT IGNORE INTO puretherapie.notificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS');
SET @boss_level = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                                  puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_level);

INSERT IGNORE INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY');
SET @boss_secretary_level = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                                  puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_level),
       (@secretary_role_id, @boss_secretary_level);

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY_MAMY');
SET @boss_secretary_mamy_level = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                                  puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_mamy_level),
       (@secretary_role_id, @boss_secretary_mamy_level),
       (@mamy_role_id, @boss_secretary_mamy_level);

INSERT INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('BOSS_SECRETARY_MAMY_TECHNICIAN');
SET @boss_secretary_mamy_technician_level = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                                  puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@boss_role_id, @boss_secretary_mamy_technician_level),
       (@secretary_role_id, @boss_secretary_mamy_technician_level),
       (@mamy_role_id, @boss_secretary_mamy_technician_level),
       (@technician_role_id, @boss_secretary_mamy_technician_level);

INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'boss', 'boss', 'boss@email.fr', 0, '+33 6 07 07 07 08', NOW(), 1);
SET @boss_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@boss_id, 'boss', 'boss');

INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'secretary', 'secretary', 'secretary@email.fr', 0, '+33 6 07 07 07 09', NOW(), 1);
SET @secretary_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@secretary_id, 'secretary', 'secretary');

INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'mamy', 'mamy', 'mamy@email.fr', 0, '+33 6 07 07 07 10', NOW(), 1);
SET @mamy_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@mamy_id, 'mamy', 'mamy');

INSERT IGNORE INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                        puretherapie.Person.email, puretherapie.Person.gender,
                                        puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('U', 'technician', 'technician', 'technician@email.fr', 0, '+33 6 07 07 07 11', NOW(), 1);
SET @technician_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.User (puretherapie.User.idPerson, puretherapie.User.username, puretherapie.User.password)
VALUES (@technician_id, 'technician', 'technician');

INSERT IGNORE INTO puretherapie.AssociationUserRole (puretherapie.AssociationUserRole.idPerson, puretherapie.AssociationUserRole.idRole)
VALUES (@boss_id, @boss_role_id),
       (@secretary_id, @secretary_role_id),
       (@mamy_id, @mamy_role_id),
       (@technician_id, @technician_role_id);
