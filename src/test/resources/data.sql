INSERT IGNORE INTO puretherapie.PersonOrigin (puretherapie.PersonOrigin.type)
VALUES ('None'),
       ('Facebook'),
       ('Groupon'),
       ('Friend');

INSERT IGNORE INTO puretherapie.NotificationLevel (puretherapie.NotificationLevel.notificationLevelName)
VALUES ('ALL_ROLES');
SET @level_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('BOSS');
SET @role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@role_id, @level_id);

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('SECRETARY');
SET @role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@role_id, @level_id);


INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('MAMY');
SET @role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@role_id, @level_id);

INSERT IGNORE INTO puretherapie.Role (puretherapie.Role.roleName)
VALUES ('TECHNICIAN');
SET @role_id = LAST_INSERT_ID();

INSERT IGNORE INTO puretherapie.AssociationRoleNotificationLevel (puretherapie.AssociationRoleNotificationLevel.idRole,
                                                           puretherapie.AssociationRoleNotificationLevel.idNotificationLevel)
VALUES (@role_id, @level_id);

