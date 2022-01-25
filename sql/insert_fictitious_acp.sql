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