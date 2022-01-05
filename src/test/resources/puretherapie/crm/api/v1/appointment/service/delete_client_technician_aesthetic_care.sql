SELECT @client_id := Person.idPerson
FROM puretherapie.Person
WHERE email = 'client@email.fr';
SELECT @technician_id := Person.idPerson
FROM puretherapie.Person
WHERE email = 'tech@email.fr';

DELETE
FROM puretherapie.Client
WHERE puretherapie.Client.idPerson = @client_id;
DELETE
FROM puretherapie.Technician
WHERE puretherapie.Technician.idPerson = @technician_id;

DELETE
FROM puretherapie.Person
WHERE puretherapie.Person.idPerson = @client_id
   OR puretherapie.Person.idPerson = @technician_id;

DELETE
FROM puretherapie.AestheticCare;