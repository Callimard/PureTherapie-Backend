/* Update bundle purchase */
ALTER TABLE puretherapie.BundlePurchase
    ADD COLUMN date DATETIME NULL;

UPDATE puretherapie.BundlePurchase bP INNER JOIN Bill bill on bP.idBill = bill.idBill
SET bP.date = bill.creationDate;

ALTER TABLE puretherapie.BundlePurchase MODIFY COLUMN date DATETIME NOT NULL;

CREATE INDEX date_bundle_purchase_idx ON puretherapie.BundlePurchase (date);

/* Update session purchase */
ALTER TABLE puretherapie.SessionPurchase ADD COLUMN date DATETIME NULL;

UPDATE puretherapie.SessionPurchase sP INNER JOIN Bill bill on sP.idBill = bill.idBill
SET sP.date = bill.creationDate;

ALTER TABLE puretherapie.SessionPurchase MODIFY COLUMN date DATETIME NOT NULL;

CREATE INDEX date_session_purchase_idx ON puretherapie.SessionPurchase (date);