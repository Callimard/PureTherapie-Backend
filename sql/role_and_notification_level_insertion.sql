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