/* Payment types */
INSERT INTO puretherapie.PaymentType (puretherapie.PaymentType.name, puretherapie.PaymentType.description)
VALUES ('ONT_TIME', 'Payment en une fois'),
       ('THREE_TIMES', 'Payement en trois fois');

/* Means of Payments */
INSERT INTO puretherapie.MeansOfPayment (puretherapie.MeansOfPayment.name, puretherapie.MeansOfPayment.description)
VALUES ('Carte bancaire', 'Paiement par catre bancaire'),
       ('Espèce', 'Paiement en espèce'),
       ('Chèque', 'Paiemetn par chèque');

/* TS atom */
INSERT INTO puretherapie.TimeSlotAtom (puretherapie.TimeSlotAtom.numberOfMinutes, puretherapie.TimeSlotAtom.effectiveDate)
VALUES (30, NOW());

/* Global Opening */
INSERT INTO puretherapie.GlobalOpeningTime (puretherapie.GlobalOpeningTime.day, puretherapie.GlobalOpeningTime.openingTime,
                                            puretherapie.GlobalOpeningTime.closeTime)
VALUES (1, '10:00:00', '21:00:00'),
       (2, '10:00:00', '21:00:00'),
       (3, '10:00:00', '21:00:00'),
       (4, '10:00:00', '21:00:00'),
       (5, '10:00:00', '21:00:00'),
       (6, '10:00:00', '21:00:00');

/* Aesthetic Cares */
INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('Le petit soin', 39.99, 30);

INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('Le soin expert', 80.50, 45);

INSERT INTO puretherapie.AestheticCare (puretherapie.AestheticCare.name, puretherapie.AestheticCare.price, puretherapie.AestheticCare.timeExecution)
VALUES ('Le soin primordial', 120.33, 60);

/* Aesthetic Care Packages */
SELECT puretherapie.AestheticCare.idAestheticCare INTO @petit_soin
FROM puretherapie.AestheticCare
WHERE name = 'Le petit soin';

SELECT puretherapie.AestheticCare.idAestheticCare INTO @expert_soin
FROM puretherapie.AestheticCare
WHERE puretherapie.AestheticCare.name = 'Le soin expert';

SELECT AestheticCare.idAestheticCare INTO @primordial_soin
FROM puretherapie.AestheticCare
WHERE puretherapie.AestheticCare.name = 'Le soin primordial';

INSERT INTO AestheticCarePackage (AestheticCarePackage.idAestheticCare, AestheticCarePackage.name, AestheticCarePackage.numberAestheticCare)
VALUES (@petit_soin, 'La semaine de petit soin', 7),
       (@expert_soin, 'La semaine de soin expert', 7),
       (@primordial_soin, 'La semaine de soin primordial', 7);

/* Bundle */
SELECT @petit := AestheticCarePackage.idAestheticCarePackage
FROM puretherapie.AestheticCarePackage
WHERE name = 'La semaine de petit soin';

SELECT @moyen := AestheticCarePackage.idAestheticCarePackage
FROM puretherapie.AestheticCarePackage
WHERE name = 'La semaine de soin expert';

SELECT @grand := AestheticCarePackage.idAestheticCarePackage
FROM puretherapie.AestheticCarePackage
WHERE name = 'La semaine de soin primordial';

INSERT INTO puretherapie.Bundle (puretherapie.Bundle.name, puretherapie.Bundle.price)
VALUES ('Petit bundle', 250);
SET @b_petit = LAST_INSERT_ID();

INSERT INTO puretherapie.Bundle (puretherapie.Bundle.name, puretherapie.Bundle.price)
VALUES ('Moyen bundle', 500);
SET @b_moyen = LAST_INSERT_ID();

INSERT INTO puretherapie.Bundle (puretherapie.Bundle.name, puretherapie.Bundle.price)
VALUES ('Grand bundle', 700);
SET @b_grand = LAST_INSERT_ID();

INSERT INTO puretherapie.Bundle (puretherapie.Bundle.name, puretherapie.Bundle.price)
VALUES ('THE SUPER bundle', 1200);
SET @b_super = LAST_INSERT_ID();

INSERT INTO puretherapie.AssociationBundleAestheticCarePackage (puretherapie.AssociationBundleAestheticCarePackage.idBundle,
                                                                puretherapie.AssociationBundleAestheticCarePackage.idAestheticCarePackage)
VALUES (@b_petit, @petit),
       (@b_moyen, @moyen),
       (@b_grand, @grand),
       (@b_super, @petit),
       (@b_super, @moyen),
       (@b_super, @grand);

/* Person Origins */
INSERT IGNORE INTO puretherapie.PersonOrigin (puretherapie.PersonOrigin.type)
VALUES ('Aucune'),
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

/* Technicians */
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
VALUES ('T', 'charlotte', 'zigwer', 'romane.boucer@puretherapie.fr', 0, '+33 6 66 66 77 00', NOW(), @none_person_origin_id);
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