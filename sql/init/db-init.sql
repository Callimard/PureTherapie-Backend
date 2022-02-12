/* Payment types */
INSERT INTO puretherapie.PaymentType (puretherapie.PaymentType.name, puretherapie.PaymentType.description)
VALUES ('ONT_TIME', 'Paiement en une fois'),
       ('THREE_TIMES', 'Paiement en trois fois');

/* Means of Payments */
INSERT INTO puretherapie.MeansOfPayment (puretherapie.MeansOfPayment.name, puretherapie.MeansOfPayment.description)
VALUES ('Carte bancaire', 'Paiement par catre bancaire'),
       ('Espèce', 'Paiement en espèce'),
       ('Chèque', 'Paiemetn par chèque');

/* TS atom */
INSERT INTO puretherapie.TimeSlotAtom (puretherapie.TimeSlotAtom.numberOfMinutes, puretherapie.TimeSlotAtom.effectiveDate)
VALUES (40, NOW());

/* Global Opening */
INSERT INTO puretherapie.GlobalOpeningTime (puretherapie.GlobalOpeningTime.day, puretherapie.GlobalOpeningTime.openingTime,
                                            puretherapie.GlobalOpeningTime.closeTime)
VALUES (2, '9:00:00', '21:00:00'),
       (3, '9:00:00', '21:00:00'),
       (4, '9:00:00', '21:00:00'),
       (5, '9:00:00', '21:00:00'),
       (6, '9:00:00', '21:00:00');

/* Aesthetic Cares */
INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('Soin découverte', 39.90, 40);

/* Aesthetic Care Packages */
SELECT puretherapie.AestheticCare.idAestheticCare INTO @petit_soin
FROM puretherapie.AestheticCare
WHERE name = 'Soin découverte';

INSERT INTO AestheticCarePackage (AestheticCarePackage.idAestheticCare, AestheticCarePackage.name, AestheticCarePackage.numberAestheticCare)
VALUES (@petit_soin, 'Package', 5);

/* Bundle */
SELECT @petit := AestheticCarePackage.idAestheticCarePackage
FROM puretherapie.AestheticCarePackage
WHERE name = 'Package';

INSERT INTO puretherapie.Bundle (puretherapie.Bundle.name, puretherapie.Bundle.price)
VALUES ('Package', 380);
SET @b_petit = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationBundleAestheticCarePackage (puretherapie.AssociationBundleAestheticCarePackage.idBundle,
                                                                puretherapie.AssociationBundleAestheticCarePackage.idAestheticCarePackage)
VALUES (@b_petit, @petit);

/* Person Origins */
INSERT IGNORE INTO puretherapie.PersonOrigin (puretherapie.PersonOrigin.type)
VALUES ('Autre'),
       ('Facebook'),
       ('Groupon'),
       ('Ami');

/* Role and Notification Level association */
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

INSERT IGNORE INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
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

/* Users */
SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin FROM puretherapie.PersonOrigin WHERE type = 'Autre';

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

/* Technicians */
SELECT @none_person_origin_id := PersonOrigin.idPersonOrigin FROM puretherapie.PersonOrigin WHERE type = 'Autre';

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'Rié', '', 'sabine.delout@puretherapie.fr', 0, '+33 6 06 06 07 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'Kurumi', '', 'marine.courtenay@puretherapie.fr', 0, '+33 6 06 06 77 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'Ayono', '', 'romane.boucer@puretherapie.fr', 0, '+33 6 66 66 77 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);

INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                 puretherapie.Person.email, puretherapie.Person.gender,
                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
VALUES ('T', 'Naida', '', 'charelene.blanche@puretherapie.fr', 0, '+33 6 76 96 07 00', NOW(), @none_person_origin_id);
SET @tech_id = LAST_INSERT_ID();

INSERT INTO puretherapie.Technician (puretherapie.Technician.idPerson)
VALUES (@tech_id);