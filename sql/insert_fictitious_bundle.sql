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